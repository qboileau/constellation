package org.constellation.ws.rest;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.constellation.configuration.AcknowlegementType;
import org.constellation.engine.register.DTOMapper;
import org.constellation.engine.register.User;
import org.constellation.engine.register.UserDTO;
import org.constellation.engine.register.repository.UserRepository;
import org.geotoolkit.util.StringUtilities;
import org.springframework.util.StringUtils;

/**
 * RestFull user configuration service
 * 
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 */
@Named
@Path("/1/user")
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public class UserService {

	@Inject
	private DTOMapper dtoMapper;

	@Inject
	private UserRepository userRepository;

	/**
	 * @return a {@link Response} which contains requester user name
	 */
	@GET
	@Path("/{id}")
	public Response findOne(@PathParam("id") String login) {
		if (StringUtils.hasText(login)) {
			User user = userRepository.findOneWithRole(login);
			UserDTO userDTO = dtoMapper.entityToDTO(user);
			return Response.ok(userDTO).build();
		}
		List<? extends User> list = userRepository.all();
		List<UserDTO> dtos = new ArrayList<UserDTO>();
		for (User user : list) {
			UserDTO userDTO = dtoMapper.entityToDTO(user);
			dtos.add(userDTO);
		}
		return Response.ok(dtos).build();
	}

	/**
	 * @return a {@link Response} which contains requester user name
	 */
	@GET
	@Path("/")
	public Response findAll(@PathParam("id") String login) {

		List<? extends User> list = userRepository.all();
		List<UserDTO> dtos = new ArrayList<UserDTO>();
		for (User user : list) {
			UserDTO userDTO = dtoMapper.entityToDTO(user);
			dtos.add(userDTO);
		}
		return Response.ok(dtos).build();
	}

	@DELETE
	@Path("/{id}")
	public Response delete(@PathParam("id") String id) {
		userRepository.delete(id);
		return Response.noContent().build();
	}

	@POST
	@Path("/")
	public Response post(UserDTO userDTO) {
		if (StringUtils.hasText(userDTO.getPassword()))
			userDTO.setPassword(StringUtilities.MD5encode(userDTO.getPassword()));
		User user = dtoMapper.dtoToEntity(userDTO);

		userRepository.save(user);
		return Response.ok(user).build();
	}

	/**
	 * Called on login. To know if login is granted to access to server
	 * 
	 * @return an {@link AcknowlegementType} on {@link Response} to know
	 *         operation state
	 */
	@GET
	@Path("/access")
	public Response access() {
		final AcknowlegementType response = new AcknowlegementType("Success",
				"You have access to the configuration service");
		return Response.ok(response).build();
	}

}
