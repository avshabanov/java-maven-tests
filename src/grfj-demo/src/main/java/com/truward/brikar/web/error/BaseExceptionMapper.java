package com.truward.brikar.web.error;

import com.truward.brikar.error.model.ErrorModel;
import com.truward.protobuf.jaxrs.ProtobufMediaType;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.function.Supplier;

/**
 * Base exception mapper class.
 *
 * @author Alexander Shabanov
 */
public abstract class BaseExceptionMapper {
  private static final MediaType DEFAULT_MEDIA_TYPE = MediaType.APPLICATION_JSON_TYPE;

  @Context
  private HttpHeaders headers;

  public Response createResponse(Supplier<ErrorModel.ErrorResponseV1> errorResponseSupplier) {
    final MediaType mediaType = getAcceptType();
    final Response.ResponseBuilder builder = Response.status(Response.Status.NOT_FOUND);

    if (mediaType != null) {
      // add entity and set type
      builder.entity(errorResponseSupplier.get());
      builder.type(mediaType);
    }

    return builder.build();
  }

  //
  // Private
  //

  private MediaType getAcceptType(){
    final List<MediaType> accepts = headers.getAcceptableMediaTypes();
    if (accepts != null && accepts.size() > 0) {
      for (final MediaType mediaType : accepts) {
        if (mediaType.isWildcardType()) {
          return DEFAULT_MEDIA_TYPE;
        }

        if (ProtobufMediaType.MEDIA_TYPE.isCompatible(mediaType) ||
            MediaType.APPLICATION_JSON_TYPE.isCompatible(mediaType)) {
          return mediaType;
        }
      }

      // can't find anything suitable
      return null;
    }

    // use default type (application/json)
    return DEFAULT_MEDIA_TYPE;
  }
}
