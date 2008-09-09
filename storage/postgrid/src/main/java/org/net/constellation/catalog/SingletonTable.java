/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.catalog;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.Set;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import org.geotools.util.WeakValueHashMap;
import org.constellation.resources.i18n.Resources;
import org.constellation.resources.i18n.ResourceKeys;


/**
 * Base class for tables with a {@code getEntry(...)} method returning at most one element. The
 * elements are uniquely identified by a name and (optionally) a numeric ID.
 * <p>
 * {@code SingletonTable} defines the {@link #getEntries()}, {@link #getEntry(String)} and
 * {@link #getEntry(int)} methods. Subclasses must provides implementation for the following
 * methods:
 * <p>
 * <ul>
 *   <li>{@link #createEntry}<br>
 *       Creates an element for the current row.</li>
 *   <li>{@link #postCreateEntry} (facultatif)<br>
 *       Performs additional element construction after the {@linkplain ResultSet result set} has
 *       been closed. This is useful if the construction may implies new queries involving the same
 *       table or statement.</li>
 * </ul>
 * <p>
 * The elements created by this class are cached for faster access the next time a
 * {@code getEntry(...)} method is invoked again.
 *
 * @param <E> The kind of element to be created by this table.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class SingletonTable<E> extends Table {
    /**
     * The maximal value (inclusive) for auto-increment. Used in order to avoid long
     * iteration over too big tables (in which case we need to find a better algorithm).
     * <p>
     * If this number is modified, consider changing the number of leading zeros as well
     * if the formatting code.
     *
     * @see #searchFreeIdentifier
     */
    private static final int MAXIMUM_AUTO_INCREMENT = 999;

    /**
     * The main parameter to use for the identification of a record, or {@code null} if
     * unknown. If non-null, this is the parameter corresponding to {@link #indexByName}
     * (the preferred key) or {@link #indexByNumber}.
     */
    private Parameter primaryKey;

    /**
     * The parameter to use for looking an element by name, or {@code 0} if unset.
     * This is usually the parameter for the value to search in the primary key column.
     *
     * @see #setIdentifierParameters
     */
    private int indexByName;

    /**
     * The parameter to use for looking an element by its numeric identifier,
     * or {@code 0} if unset.
     *
     * @see #setIdentifierParameters
     */
    private int indexByNumber;

    /**
     * The elements created up to date. The key should be {@link Integer} or {@link String}
     * instances only.
     * <p>
     * Note: L'utilisation d'une cache n'est pas forcément souhaitable. Si la base de données a été
     *       mise à jour après qu'une entrée est été mise dans la cache, la mise-à-jour ne sera pas
     *       visible. Une solution possible serait de prévoir un Listener à appeller lorsque la base
     *       de données a été mise à jour.
     */
    private final Map<Object,E> pool = new WeakValueHashMap<Object,E>();

    /**
     * Creates a new table using the specified query. The query given in argument should be some
     * subclass with {@link Query#addColumn addColumn} and {@link Query#addParameter addParameter}
     * methods invoked in its constructor.
     *
     * @param query The query to use for this table.
     */
    protected SingletonTable(final Query query) {
        super(query);
    }

    /**
     * Creates a new table connected to the same {@linkplain #getDatabase database} and using
     * the same {@linkplain #query query} than the specified table. Subclass constructors should
     * not modify the query, since it is shared.
     *
     * @param table The table to use as a template.
     */
    protected SingletonTable(final SingletonTable<E> table) {
        super(table);
        primaryKey    = table.primaryKey;
        indexByName   = table.indexByName;
        indexByNumber = table.indexByNumber;
    }

    /**
     * Sets the parameter to use for looking an element by identifier. This is usually the
     * parameter for the value to search in the primary key column. This information is needed
     * for {@link #getEntry(String)} execution and is usually specified at construction time.
     *
     * @param  byName   The parameter for looking an element by name, or {@code null} if none.
     * @param  byNumber The parameter for looking an element by its numeric identifier, or
     *                  {@code null} if none. Most table do not provide a numeric identifer.
     * @throws IllegalArgumentException if the specified parameters are not one of those
     *         declared for {@link QueryType#SELECT} or {@link QueryType#SELECT_BY_NUMBER}.
     */
    protected synchronized void setIdentifierParameters(final Parameter byName, final Parameter byNumber)
            throws IllegalArgumentException
    {
        int   newByName = 0;
        int newByNumber = 0;
        boolean success = true;
        String    name = "byName";
        Parameter param = byName;
        final Parameter newPK;
        if (byName == null) {
            newPK = byNumber;
            if (byNumber != null) {
                newByName = byNumber.indexOf(QueryType.SELECT);
                // Optional, so don't test for success.
            }
        } else {
            newPK = byName;
            if (success = query.getParameters(QueryType.SELECT).contains(byName)) {
                newByName = byName.indexOf(QueryType.SELECT);
                success = (newByName != 0);
            }
        }
        if (success) {
            name = "byNumber";
            param = byNumber;
            if (byNumber == null) {
                if (byName != null) {
                    newByNumber = byName.indexOf(QueryType.SELECT_BY_NUMBER);
                    // Optional, so don't test for success.
                }
            } else {
                if (success = query.getParameters(QueryType.SELECT_BY_NUMBER).contains(byNumber)) {
                    newByNumber = byNumber.indexOf(QueryType.SELECT_BY_NUMBER);
                    success = (newByNumber != 0);
                }
            }
        }
        if (!success) {
            throw new IllegalArgumentException(Resources.format(ResourceKeys.ERROR_BAD_ARGUMENT_$2, name, param));
        }
        if ((newByName != indexByName) || (newByNumber != indexByNumber) || (newPK != primaryKey)) {
            primaryKey    = newPK;
            indexByName   = newByName;
            indexByNumber = newByNumber;
            flush();
            fireStateChanged("identifierParameters");
        }
    }

    /**
     * Invokes the user's {@link #createEntry(ResultSet)} method, but wraps {@link SQLException}
     * into {@link ServerException} because the later provides more informations.
     */
    private E createEntry(final ResultSet results, final Object key, final int index)
            throws CatalogException, SQLException
    {
        try {
            return createEntry(results);
        } catch (CatalogException exception) {
            if (!exception.isMetadataInitialized()) {
                exception.setMetadata(this, results, index, (key!=null) ? key.toString() : null);
                exception.clearColumnName();
            }
            throw exception;
        } catch (SQLException cause) {
            final ServerException exception = new ServerException(cause);
            exception.setMetadata(this, results, index, (key!=null) ? key.toString() : null);
            exception.clearColumnName();
            throw exception;
        }
    }

    /**
     * Creates an {@link Element} object for the current {@linkplain ResultSet result set} row.
     * This method is invoked automatically by {@link #getEntry(String)} and {@link #getEntries()}.
     *
     * @param  results  The result set to use for fetching data. Only the current row should be
     *                  used, i.e. {@link ResultSet#next} should <strong>not</strong> be invoked.
     * @return The element for the current row in the specified {@code results}.
     * @throws CatalogException if a logical error has been detected in the database content.
     * @throws SQLException if an error occured will reading from the database.
     */
    protected abstract E createEntry(final ResultSet results) throws CatalogException, SQLException;

    /**
     * Completes the creation of the specified element. This method is invoked after the
     * {@link ResultSet} has been closed, which allow recursive call to {@code getEntry(...)}.
     * The default implementation do nothing.
     *
     * @param  entry The element to complete.
     * @throws CatalogException if a logical error has been detected in the database content.
     * @throws SQLException if an error occured will reading from the database.
     */
    protected void postCreateEntry(final E entry) throws CatalogException, SQLException {
    }

    /**
     * Returns {@code true} if {@link #getEntries} should accept the given element.
     * The default implementation always returns {@code true}.
     *
     * @param  entry En element created by {@link #getEntries}.
     * @return {@code true} if the element should be added to the set returned by {@link #getEntries}.
     * @throws CatalogException if a logical error has been detected in the database content.
     * @throws SQLException if an error occured will reading from the database.
     */
    protected boolean accept(final E entry) throws CatalogException, SQLException {
        return true;
    }

    /**
     * Adds the specified entry in the map, while making sure that it wasn't already there.
     */
    private void cache(final Object key, final E entry) {
        assert (key instanceof String) || (key instanceof Integer) : key;
        if (pool.put(key, entry) != null) {
            throw new AssertionError(key);
        }
    }

    /**
     * Returns a single entry for the specified {@code statement}. All SQL parameters must have been
     * set on the prepared {@code statement} before this method is invoked. This method assumes that
     * the caller has already checked that there is not entry in the cache for the given key.
     * <p>
     * This method executes the query, invokes {@link #createEntry}, puts the result in the
     * cache, close the result set and finally invokes {@link #postCreateEntry}.
     *
     * @param  statement The statement to execute.
     * @param  key The primary key for the record to look for.
     * @param  index The primary key column. Used mostly for formatting error messages.
     * @return The record (never {@code null}).
     * @throws NoSuchRecordException if no record was found for the specified key.
     * @throws CatalogException if a logical error has been detected in the database content.
     * @throws SQLException if a SQL error occured while reading the database.
     *
     * @todo Contains an unsafe cast.
     */
    private E executeQuery(final PreparedStatement statement, Object key, final int index)
            throws CatalogException, SQLException
    {
        assert Thread.holdsLock(this);
        assert !pool.containsKey(key) : key;
        E entry = null;
        final ResultSet results = statement.executeQuery();
        while (results.next()) {
            final E candidate = createEntry(results, key, index);
            if (entry == null) {
                entry = candidate;
            } else if (!entry.equals(candidate)) {
                throw new DuplicatedRecordException(this, results, index, String.valueOf(key));
            }
        }
        if (entry == null) {
            throw new NoSuchRecordException(this, results, index, String.valueOf(key));
        }
        results.close();
        /*
         * Element creation is now finished. Stores under the specified key, which may be the
         * name (as a String) or the identifier (as an Integer). If the key is the entry's
         * name, uses the entry's String instance in order to reduce the number of objects
         * to be managed by the garbage collector.
         */
        final String name = ((Entry)entry).getName(); // TODO: unsafe cast.
        if (key.equals(name)) {
            key = name; // Use the same instance (slight memory saver).
        } else {
            // Maybe the key is an Integer and an object already exists in the pool using the
            // key as a String. Checks using the String key, and add the Integer key as an alias.
            final E candidate = pool.get(name);
            if (candidate != null) {
                cache(key, candidate);
                return candidate;
            } else {
                cache(name, entry);
            }
        }
        cache(key, entry);
        /*
         * Termine la construction en appellant postCreateEntry(...), mais seulement après avoir
         * placé le résultat dans la cache car postCreateEntry(...) peut appeller getEntry(...) de
         * manière récursive pour la même clé. En cas d'échec, on retirera l'entrée de la cache.
         */
        try {
            postCreateEntry(entry);
        } catch (CatalogException exception) {
            roolback(name, key);
            throw exception;
        } catch (SQLException exception) {
            roolback(name, key);
            throw exception;
        } catch (RuntimeException exception) {
            roolback(name, key);
            throw exception;
        }
        return entry;
    }

    /**
     * Returns an element for the specified ID number. This method is often similar to
     * <code>{@link #getEntry(String) getEntry}(String.valueOf(identifier))</code>,
     * but some subclasses (typically the ones that implement the {@link NumericAccess}
     * interface) may be more efficient.
     *
     * @param  identifier The numeric identifier of the element to fetch.
     * @return The element for the given numeric identifier.
     * @throws NoSuchRecordException if no record was found for the specified key.
     * @throws CatalogException if a logical error has been detected in the database content.
     * @throws SQLException if an error occured will reading from the database.
     */
    public synchronized E getEntry(final int identifier)
            throws NoSuchRecordException, CatalogException, SQLException
    {
        final Integer key = identifier; // Autoboxing
        E entry = pool.get(key);
        if (entry != null) {
            return entry;
        }
        if (indexByNumber == 0) {
            throw new IllegalStateException();
        }
        final PreparedStatement statement = getStatement(QueryType.SELECT_BY_NUMBER);
        statement.setInt(indexByNumber, identifier);
        return executeQuery(statement, key, indexByNumber);
    }

    /**
     * Returns an element for the given name.
     *
     * @param  name The name of the element to fetch.
     * @return The element for the given name, or {@code null} if {@code name} was null.
     * @throws NoSuchRecordException if no record was found for the specified key.
     * @throws CatalogException if a logical error has been detected in the database content.
     * @throws SQLException if an error occured will reading from the database.
     */
    public synchronized E getEntry(String name)
            throws NoSuchRecordException, CatalogException, SQLException
    {
        if (name == null) {
            return null;
        }
        name = name.trim();
        E entry = pool.get(name);
        if (entry != null) {
            return entry;
        }
        if (indexByName == 0) {
            throw new IllegalStateException();
        }
        final PreparedStatement statement = getStatement(QueryType.SELECT);
        statement.setString(indexByName, name);
        return executeQuery(statement, name, indexByName);
    }

    /**
     * Returns all entries available in the database. The returned set may or may not be
     * serializable or modifiable, at implementation choice. If allowed, modification in
     * the returned set will not alter this table.
     *
     * @return The set of entries. May be empty, but neven {@code null}.
     * @throws CatalogException if a logical error has been detected in the database content.
     * @throws SQLException if an error occured will reading from the database.
     */
    public synchronized Set<E> getEntries() throws CatalogException, SQLException {
        return getEntries(QueryType.LIST);
    }

    /**
     * Returns all entries available in the database using the specified query type.
     *
     * @param  type The query type, usually {@link QueryType#LIST}.
     * @return The set of entries. May be empty, but neven {@code null}.
     * @throws CatalogException if a logical error has been detected in the database content.
     * @throws SQLException if an error occured will reading from the database.
     *
     * @todo Contains a unsafe cast.
     */
    protected Set<E> getEntries(final QueryType type) throws CatalogException, SQLException {
        assert Thread.holdsLock(this);
        final Map<E,Boolean> set = new LinkedHashMap<E,Boolean>();
        final PreparedStatement statement = getStatement(type);
        final ResultSet results = statement.executeQuery();
        try {
            while (results.next()) {
                E entry = createEntry(results, null, indexByName);
                if (accept(entry)) {
                    final String name = ((Entry)entry).getName();// TODO entry.getName();
                    /*
                     * Si une entrée existait déjà dans la cache, réutilise cette entrée en se
                     * souvenant que postCreateEntry(...) n'a pas besoin d'être appelée pour elle.
                     */
                    Boolean initialized = Boolean.FALSE;
                    final E previous = pool.get(name);
                    if (previous != null) {
                        entry       = previous;
                        initialized = Boolean.TRUE;
                    } else {
                        cache(name, entry);
                    }
                    if (set.put(entry, initialized) != null) {
                        throw new DuplicatedRecordException(this, results, indexByName, name);
                    }
                }
            }
            results.close();
            for (final Map.Entry<E,Boolean> entry : set.entrySet()) {
                if (!entry.getValue().booleanValue()) {
                    postCreateEntry(entry.getKey());
                    entry.setValue(Boolean.TRUE); // Mark as initialized.
                }
            }
        } catch (CatalogException exception) {
            roolback(set);
            throw exception;
        } catch (SQLException exception) {
            roolback(set);
            throw exception;
        } catch (RuntimeException exception) {
            roolback(set);
            throw exception;
        }
        return set.keySet();
    }

    /**
     * Removes the given entries from the cache. This is invoked when an exception
     * occured inside the {@link #executeQuery} method.
     */
    private void roolback(final String name, final Object key) {
        pool.remove(name);
        pool.remove(key);
    }

    /**
     * Retire de la cache toutes les entrées qui n'avaient pas encore été initialisées.
     * Cette méthode est appelée en cas d'erreur à l'intérieur de {@link #getEntries}
     * afin de conserver {@link SingletonTable} dans un état récupérable.
     */
    private void roolback(final Map<E,Boolean> set) {
        for (final Map.Entry<E,Boolean> entry : set.entrySet()) {
            if (!entry.getValue().booleanValue()) {
                pool.remove(((Entry)entry.getKey()).getName());
            }
        }
    }

    /**
     * Returns the names of all entries available in the database. This method is much
     * more economical than {@link #getEntries} when only the identifiers are wanted.
     *
     * @return The set of entry identifiers. May be empty, but neven {@code null}.
     * @throws CatalogException if a logical error has been detected in the database content.
     * @throws SQLException if an error occured will reading from the database.
     */
    public synchronized Set<String> getIdentifiers() throws CatalogException, SQLException {
        return getIdentifiers(QueryType.LIST);
    }

    /**
     * Returns the names of all entries available in the database using the specified query type.
     *
     * @param  The query type, usually {@link QueryType#LIST}.
     * @return The set of entry identifiers. May be empty, but neven {@code null}.
     * @throws CatalogException if a logical error has been detected in the database content.
     * @throws SQLException if an error occured will reading from the database.
     */
    private Set<String> getIdentifiers(final QueryType type) throws CatalogException, SQLException {
        assert Thread.holdsLock(this);
        final int index = (indexByName != 0) ? indexByName : indexByNumber;
        final Set<String> identifiers = new LinkedHashSet<String>();
        final ResultSet results = getStatement(type).executeQuery();
        while (results.next()) {
            identifiers.add(results.getString(index));
        }
        results.close();
        return identifiers;
    }

    /**
     * Checks if an element exists for the given name. This method do not attempt to create
     * the element and doesn't check if the entry is valid.
     *
     * @param  name The name of the element to fetch.
     * @return {@code true} if an element of the given name was found.
     * @throws CatalogException if a logical error has been detected in the database content.
     * @throws SQLException if an error occured will reading from the database.
     */
    public synchronized boolean exists(String name) throws CatalogException, SQLException {
        if (name == null) {
            return false;
        }
        name = name.trim();
        if (pool.containsKey(name)) {
            return true;
        }
        final PreparedStatement statement = getStatement(QueryType.EXISTS);
        statement.setString(indexOf(primaryKey), name);
        final ResultSet results = statement.executeQuery();
        final boolean hasNext = results.next();
        results.close();
        return hasNext;
    }

    /**
     * Deletes the elements for the given name.
     *
     * @param  name The name of the element to delete.
     * @return The number of elements deleted.
     * @throws CatalogException if a logical error has been detected in the database content.
     * @throws SQLException if an error occured will reading from or writting to the database.
     */
    public synchronized int delete(String name) throws CatalogException, SQLException {
        if (name == null) {
            return 0;
        }
        name = name.trim();
        final int count;
        boolean success = false;
        transactionBegin();
        try {
            final PreparedStatement statement = getStatement(QueryType.DELETE);
            statement.setString(indexOf(primaryKey), name);
            count = update(statement);
            success = true;
        } finally {
            transactionEnd(success);
        }
        // Updates the cache. Note that it is safe to execute this update
        // last since we rolled back the transaction in case of failure.
        pool.remove(name);
        return count;
    }

    /**
     * Deletes many elements. "Many" depends on the configuration set by {@link #configure}.
     * It may be the whole table. Note that the this action may be blocked if the user doesn't
     * have the required database authorisations, or if some records are still referenced in
     * foreigner tables.
     *
     * @return The number of elements deleted.
     * @throws CatalogException if a logical error has been detected in the database content.
     * @throws SQLException if an error occured will reading from or writting to the database.
     */
    public synchronized int clear() throws CatalogException, SQLException {
        final int count;
        boolean success = false;
        transactionBegin();
        try {
            final PreparedStatement statement = getStatement(QueryType.CLEAR);
            count = update(statement);
            success = true;
        } finally {
            transactionEnd(success);
        }
        // Updates the cache. Note that it is safe to execute this update
        // last since we rolled back the transaction in case of failure.
        pool.clear();
        return count;
    }

    /**
     * Executes the specified SQL {@code INSERT}, {@code UPDATE} or {@code DELETE} statement.
     * As a special case, this method do not execute the statement during testing and debugging
     * phases. In the later case, this method rather prints the statement to the stream specified
     * to {@link Database#setUpdateSimulator}.
     *
     * @param  statement The statement to execute.
     * @return The number of elements updated.
     * @throws CatalogException if {@link #transactionBegin} has not been invoked
     *         at least once before this method is invoked.
     * @throws SQLException if an error occured.
     */
    private int update(final PreparedStatement statement) throws SQLException {
        assert Thread.holdsLock(this);
        final Database database = getDatabase();
        database.ensureOngoingTransaction();
        final PrintWriter out = database.getUpdateSimulator();
        if (out != null) {
            out.println(statement);
            return 0;
        } else {
            return statement.executeUpdate();
        }
    }

    /**
     * Executes the specified SQL {@code INSERT}, {@code UPDATE} or {@code DELETE} statement,
     * which is expected to insert exactly one record. As a special case, this method do not
     * execute the statement during testing and debugging phases. In the later case, this method
     * rather prints the statement to the stream specified to {@link Database#setUpdateSimulator}.
     *
     * @param  statement The statement to execute.
     * @return {@code true} if the singleton has been found and updated.
     * @throws IllegalUpdateException if more than one elements has been updated.
     * @throws SQLException if an error occured.
     */
    protected final boolean updateSingleton(final PreparedStatement statement)
            throws IllegalUpdateException, SQLException
    {
        final int count = update(statement);
        if (count > 1) {
            throw new IllegalUpdateException(count);
        }
        return count != 0;
    }

    /**
     * Searchs for an identifier not already in use. If the given string is not in use, then
     * it is returned as-is. Otherwise, this method appends a decimal number to the specified
     * base and check if the resulting identifier is not in use. If it is, then the decimal
     * number is incremented until a unused identifier is found.
     *
     * @param  base The base for the identifier.
     * @return A unused identifier.
     * @throws CatalogException if the maximal amount of identifiers has been reached.
     * @throws SQLException if an error occured while reading the database.
     */
    protected String searchFreeIdentifier(final String base) throws CatalogException, SQLException {
        final PreparedStatement statement    = getStatement(QueryType.EXISTS);
        final int               byPrimaryKey = indexOf(primaryKey);
        final int               indexPK      = primaryKey.column.indexOf(QueryType.EXISTS);
        final StringBuilder     buffer       = new StringBuilder(base.trim());
        final int               offset       = buffer.length();
scan:   for (int n=0; n<=MAXIMUM_AUTO_INCREMENT; n++) {
            if (n != 0) {
                buffer.setLength(offset);
                buffer.append('-');
                if (n < 100) buffer.append('0');
                if (n <  10) buffer.append('0');
                buffer.append(n);
            }
            final String ID = buffer.toString();
            statement.setString(byPrimaryKey, ID);
            final ResultSet results = statement.executeQuery();
            while (results.next()) {
                if (indexPK != 0) {
                    /*
                     * Below is a paranoiac check that should never be needed when the SQL
                     * statement is something like "WHERE name='foo'".  However we perform
                     * this check as an anticipation for a future version that may implement
                     * a more elaborated mechanism on top of regular expressions.
                     */
                    final String existing = results.getString(indexPK).trim();
                    if (!ID.equals(existing)) {
                        continue;
                    }
                }
                // A match has been found. Increment the counter and try an other identifier.
                results.close();
                continue scan;
            }
            // No match found. We can use this identifier.
            results.close();
            return ID;
        }
        throw new CatalogException("Trop d'itérations.");
    }

    /**
     * Clears this table cache. Subclasses should invoke this method when the table
     * {@linkplain #fireStateChanged state changed} in some way that affect the
     * {@linkplain #createEntry entries to be created}, not just the set of entries
     * to be returned.
     */
    @Override
    public synchronized void flush() {
        pool.clear();
        super.flush();
    }
}
