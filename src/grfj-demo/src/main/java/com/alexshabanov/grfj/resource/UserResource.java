package com.alexshabanov.grfj.resource;

import com.truward.orion.user.service.model.UserModel;
import com.truward.protobuf.jaxrs.ProtobufMediaType;
import org.jboss.resteasy.annotations.GZIP;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("rest/user")
@Produces({ProtobufMediaType.MIME, MediaType.APPLICATION_JSON})
@GZIP
public final class UserResource {

  @GET
  @Path("{id}")
  public UserModel.UserAccount getById(@PathParam("id") long id) {
    if (0L == id) {
      return UserModel.UserAccount.getDefaultInstance();
    }

    return UserModel.UserAccount.newBuilder()
        .setId(id)
        .setNickname("Echo1")
        .setPasswordHash("pC3vOskl0HuaYPnVX4UVHJmFOrILgk2DPn7fN84iii2rg1k325zqeYAlSGuZU1bA")
        .addContacts(UserModel.Contact.newBuilder().setNumber("+1 123 456 78 90").setType(UserModel.ContactType.PHONE).build())
        .addContacts(UserModel.Contact.newBuilder().setNumber("test@localhost").setType(UserModel.ContactType.EMAIL).build())
        .addContacts(UserModel.Contact.newBuilder().setNumber("sample@demo").setType(UserModel.ContactType.EMAIL).build())
        .addContacts(UserModel.Contact.newBuilder().setNumber("urn:twitter:12345678").setType(UserModel.ContactType.UNKNOWN).build())
        .addAuthorities("ROL E_USER")
        .addAuthorities("ROLE_READER")
        .addAuthorities("ROLE_UPLOADER")
        .setCreated(System.currentTimeMillis())
        .setActive(true)
        .build();
  }
}
