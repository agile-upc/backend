package com.agrotech.api.post.interfaces.rest.transform;

import com.agrotech.api.post.domain.model.commands.UpdatePostCommand;
import com.agrotech.api.post.interfaces.rest.resources.UpdatePostResource;

public class UpdatePostCommandFromResourceAssembler {
    public static UpdatePostCommand toCommandFromResource(Long id, UpdatePostResource resource) {
        return new UpdatePostCommand(
                id,
                resource.title(),
                resource.description(),
                resource.image());
    }
}
