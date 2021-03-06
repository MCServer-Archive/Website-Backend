package com.mcserverarchive.archive.dtos.out;

import com.mcserverarchive.archive.model.File;
import com.mcserverarchive.archive.model.Resource;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public class SimpleResourceDto {

    private int id;
    private final String name;
    private final String blurb;

    private final int totalDownloads;
    private final byte[] logo;

    private final List<UpdateDto> updates;

    public static SimpleResourceDto create(Resource resource, int totalDownloads) {
        return new SimpleResourceDto(resource.getId(), resource.getName(), resource.getBlurb(), totalDownloads, resource.getLogo(),
                getUpdates(resource.getFiles()));
    }

    private static List<UpdateDto> getUpdates(List<File> files) {
        List<UpdateDto> updateDtos = new ArrayList<>();
        for(File file : files) {
            updateDtos.add(UpdateDto.create(file));
        }
        return updateDtos;
    }
}
