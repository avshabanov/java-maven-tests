package com.truward.brikar.web.error;

import com.truward.brikar.error.RestErrors;
import com.truward.brikar.error.StandardRestErrorCode;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * @author Alexander Shabanov
 */
@Provider
public final class NotFoundExceptionMapper extends BaseExceptionMapper implements ExceptionMapper<NotFoundException> {

  @Override
  public Response toResponse(NotFoundException exception) {
    return createResponse(() ->
        RestErrors.response(exception, "Not Found", StandardRestErrorCode.UNCATEGORIZED));
  }
}
