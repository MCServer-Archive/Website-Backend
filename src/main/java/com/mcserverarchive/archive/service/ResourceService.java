package com.mcserverarchive.archive.service;

import com.mcserverarchive.archive.config.custom.SiteConfig;
import com.mcserverarchive.archive.config.exception.RestErrorCode;
import com.mcserverarchive.archive.config.exception.RestException;
import com.mcserverarchive.archive.dtos.in.CreateResourceRequest;
import com.mcserverarchive.archive.dtos.out.SimpleResourceDto;
import com.mcserverarchive.archive.model.Account;
import com.mcserverarchive.archive.model.Resource;
import com.mcserverarchive.archive.repositories.ResourceRepository;
import com.mcserverarchive.archive.repositories.UpdateRepository;
import com.mcserverarchive.archive.util.ImageUtil;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ResourceService {
    private final ResourceRepository resourceRepository;
    private final UpdateRepository updateRepository;
    private final SiteConfig siteConfig;

    public Page<SimpleResourceDto> searchResources(Pageable pageable, Predicate query) {
        return this.resourceRepository.findAll(query, pageable)
            .map(resource -> {
                int totalDownloads = this.updateRepository.getTotalDownloads(resource.getId()).orElse(0);
                return SimpleResourceDto.create(resource, totalDownloads);
            });
    }

    public Resource getResource(int resourceId) {
        Optional<Resource> resource = this.resourceRepository.findById(resourceId);

        if(resource.isEmpty()) return null;

        resource.get().setAuthor(null);
        return resource.get();
    }

    //TODO: More sanity checks
    public Resource createResource(CreateResourceRequest request) throws RestException {
        if (this.resourceRepository.existsByNameEqualsIgnoreCase(request.getName()))
            throw new RestException(RestErrorCode.RESOURCE_NAME_NOT_AVAILABLE);
        if (request.getName().isEmpty() || request.getBlurb().isEmpty() || request.getDescription().isEmpty())
            throw new RestException(RestErrorCode.REQUIRED_ARGUMENTS_MISSING);

        Resource resource = new Resource(request.getName(), request.getDescription(),
                request.getBlurb(), request.getSource(),
                request.getAuthor(), request.getCategory());

        return this.resourceRepository.save(resource);
    }

    // TODO: More sanity checks
    // todo: also this is horrible, can we just use querydsl?
    public Resource updateResource(Account account, int resourceId, MultipartFile file, CreateResourceRequest request) throws RestException {
        Resource resource = this.resourceRepository.findById(resourceId).orElseThrow(() -> new RestException(RestErrorCode.RESOURCE_NOT_FOUND));
        //if (resource.getAuthor().getId() != account.getId()) throw new RestException(RestErrorCode.FORBIDDEN);

        String name = request.getName();
        if (name != null && !name.isEmpty())
            resource.setName(name);

        String description = request.getDescription();
        if (description != null && !description.isEmpty())
            resource.setDescription(description);

        String blurb = request.getBlurb();
        if (blurb != null && !blurb.isEmpty())
            resource.setBlurb(blurb);

        String category = request.getCategory();
        if (category != null && !category.isEmpty())
            resource.setCategory(category);

        String source = request.getSource();
        if (source != null && !source.isEmpty())
            resource.setSource(source);

        if (file != null && !file.isEmpty()) {
            if (file.getSize() > this.siteConfig.getMaxUploadSize().toBytes()) throw new RestException(RestErrorCode.FILE_TOO_LARGE);
            if (file.getContentType() == null || !file.getContentType().contains("image")) throw new RestException(RestErrorCode.WRONG_FILE_TYPE);

            resource.setLogo(ImageUtil.handleImage(file));
        }

        return this.resourceRepository.save(resource);
    }
}
