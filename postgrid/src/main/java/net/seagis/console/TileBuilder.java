/*
 * (C) 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.seagis.console;

import java.awt.Point;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.logging.Level;
import javax.imageio.ImageWriteParam;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageWriterSpi;

import org.geotools.geometry.Envelope2D;
import org.geotools.image.io.mosaic.Tile;
import org.geotools.image.io.mosaic.TileManager;
import org.geotools.image.io.mosaic.MosaicBuilder;
import org.geotools.image.io.mosaic.TileManagerFactory;
import org.geotools.util.FrequencySortedSet;
import org.geotools.console.Option;
import org.geotools.resources.image.ImageUtilities;
import org.geotools.console.ExternalyConfiguredCommandLine;

import net.seagis.catalog.Database;
import net.seagis.catalog.CatalogException;
import net.seagis.coverage.catalog.WritableGridCoverageTable;


/**
 * Creates tiles and write the entries in the database. This is only a convenience utility
 * for creating tiles and filling the database from a set of source images. The database is
 * <strong>not</strong> required to contains tiles created by this utility, and the tiles
 * layout in the database are <strong>not</strong> restricted to the default layout used by
 * this utility class.
 * <p>
 * The {@linkplain #main main} method expects a {@code .properties} file describing the source
 * tiles and their layout. An example is given <a href="doc-files/TileBuilder.properties>here</a>.
 * <p>
 * By default, this utility just display a table of tiles that would be written or inserted in
 * the database. For processing to the actual tile creation or database update, one or many of
 * the following options must be specified:
 * <p>
 * <ul>
 *   <li>{@code -write-to-disk} writes the tiles to the target directory.</li>
 *   <li>{@code -fill-database} inserts the tiles metadata in the database.</li>
 * </ul>
 *
 * @author Cédric Briançon
 * @author Martin Desruisseaux
  */
public class TileBuilder extends ExternalyConfiguredCommandLine {
    /**
     * The prefix for tiles in the {@linkplain #properties} map.
     */
    private static final String TILE_PREFIX = "tile.";

    /**
     * Directory extracted from the {@linkplain #properties}. The source directory contains
     * the tiles of an existing mosaic, typically as very big tiles. The target directory is
     * the place where to write the tiles generated by this class.
     */
    protected final File sourceDirectory, targetDirectory;

    /**
     * The geographic envelope, or {@code null} if unspecified.
     */
    protected final Envelope2D envelope;

    /**
     * The target tile size, or {@code null} if unspecified.
     */
    protected final Dimension tileSize;

    /**
     * The format of the tile to be written, or {@code null} if unspecified.
     */
    protected final String format;

    /**
     * The series for the tiles to be inserted in the database.
     */
    protected final String series;

    /**
     * Flags specified in the properties file.
     */
    private final boolean keepLayout, compress;

    /**
     * Flag specified on the command lines.
     */
    @Option(name="write-to-disk", description="Write the tiles to the target directory.")
    private boolean writeToDisk;

    /**
     * Flag specified on the command lines.
     */
    @Option(name="fill-database", description="Insert the tiles metadata in the database.")
    private boolean fillDatabase;

    /**
     * Flag specified on the command lines.
     */
    @Option(description="Print the SQL statements rather than executing them (for debugging only).")
    private boolean pretend;

    /**
     * Disables JAI codecs. Experience shows that the coded bundled in J2SE work better.
     */
    static {
        ImageUtilities.allowNativeCodec("PNG", ImageReaderSpi.class, false);
        ImageUtilities.allowNativeCodec("PNG", ImageWriterSpi.class, false);
    }

    /**
     * Creates a new builder. In case of failure, a message is printed to the
     * {@link #err standard error stream} and this method invokes {@link System#exit}.
     *
     * @param args The command line arguments.
     */
    protected TileBuilder(final String[] args) {
        super(args);
        sourceDirectory = getFile     ("SourceDirectory");
        targetDirectory = getFile     ("TargetDirectory");
        envelope        = getEnvelope ("MosaicEnvelope" );
        tileSize        = getDimension("TileSize"       );
        format          = getString   ("Format"         );
        series          = getString   ("Series"         );
        keepLayout      = getBoolean  ("KeepLayout", false);
        compress        = getBoolean  ("Compress",   true);
    }

    /**
     * Process to the command execution.
     */
    public void run() {
        final Collection<Tile> tiles = createSourceTiles();
        ensureEmptyProperties();
        if (tiles.isEmpty()) {
            err.println("At least one tile must be specified.");
            System.exit(BAD_CONTENT_EXIT_CODE);
        }
        createTargetTiles(tiles);
        out.flush();
        err.flush();
    }

    /**
     * Creates the collection of source tiles. The default implementation builds the collection
     * from the files declared in the property file given on the command line. Subclasses can
     * override this method in order to complete the collection in an other way.
     *
     * @return A collection of tiles. If this method fails, a message is printed to the
     *         {@link System#err standard error stream} and {@link System#exit} is invoked.
     */
    protected Collection<Tile> createSourceTiles() {
        final Collection<Tile> tiles = new HashSet<Tile>();
        final String[] keys = properties.keySet().toArray(new String[properties.size()]);
        for (String file : keys) {
            if (file.startsWith(TILE_PREFIX)) {
                final Point origin = getPoint(file);
                file = file.substring(TILE_PREFIX.length());
                final Tile tile = new Tile(null, new File(sourceDirectory, file), 0, origin, null);
                tiles.add(tile);
            }
        }
        return tiles;
    }

    /**
     * Creates the target tiles, optionnaly writes them to disk and to the database.
     */
    private boolean createTargetTiles(Collection<Tile> tiles) {
        if (!targetDirectory.isDirectory()) {
            err.print(targetDirectory.getPath());
            err.println(" is not a directory.");
            return false;
        }
        /*
         * From the big tiles declared in the property files, infers a set of smaller tiles at
         * different overview levels. For example starting with 6 BlueMarble tiles, we can get
         * 4733 tiles of size 960 x 960 pixels. The result is printed to the standard output.
         */
        MosaicBuilder builder = new MosaicBuilder() {
            @Override
            protected void onTileWrite(Tile tile, ImageWriteParam parameters) throws IOException {
                if (!compress) {
                    parameters.setCompressionMode(ImageWriteParam.MODE_DISABLED);
                }
            }
        };
        builder.setLogLevel(Level.INFO);
        if (tileSize != null) {
            builder.setTileSize(tileSize);
        }
        builder.setTileDirectory(targetDirectory);
        builder.setMosaicEnvelope(envelope);
        builder.setTileReaderSpi(format);
        final TileManager tileManager;
        if (keepLayout) {
            tileManager = TileManagerFactory.DEFAULT.create(tiles)[0];
        } else try {
            tileManager = builder.createTileManager(tiles, 0, writeToDisk && !pretend);
        } catch (IOException e) {
            err.println(e);
            return false;
        }
        tiles = tileManager.getTiles();
        /*
         * Creates a global tiles which cover the whole area.
         * We will use the most frequent file suffix for this tile.
         *
         * TODO: probably a bad idea - WritableGridCoverageTable will no accepts arbitrary suffix,
         *       but only the suffix expected by the series. We will need to revisit this policy.
         */
        final SortedSet<String> suffixes = new FrequencySortedSet<String>(true);
        for (final Tile tile : tiles) {
            final Object input = tile.getInput();
            if (input instanceof File) {
                final String file = ((File) input).getName();
                final int split = file.lastIndexOf('.');
                if (split >= 0) {
                    suffixes.add(file.substring(split));
                }
            }
        }
        String name = series;
        if (!suffixes.isEmpty()) {
            name += suffixes.first();
        }
        final Tile global;
        try {
            global = tileManager.createGlobalTile(null, name, 0);
        } catch (IOException e) {
            err.println(e);
            return false;
        }
        /*
         * Fills the database if requested by the user. The tiles entries will be inserted in the
         * "Tiles" table while the global entry will be inserted into the "GridCoverages" table.
         */
        out.println(tileManager);
        if (fillDatabase) try {
            final Database database = new Database();
            if (pretend) {
                database.setUpdateSimulator(out);
            }
            final WritableGridCoverageTable table = new WritableGridCoverageTable(
                    database.getTable(WritableGridCoverageTable.class));
            table.setCanInsertNewLayers(true);
            table.setLayer(series);
            table.addEntry(global);
            table.addTiles(tiles);
            database.close();
        } catch (IOException e) {
            err.println(e);
            return false;
        } catch (SQLException e) {
            err.println(e);
            return false;
        } catch (CatalogException e) {
            err.println(e);
            e.printStackTrace();
            return false;
        }
        out.flush();
        return true;
    }

    /**
     * Runs from the command line.
     */
    public static void main(String[] args) {
        final TileBuilder builder = new TileBuilder(args);
        builder.run();
    }
}
