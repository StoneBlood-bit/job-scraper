package job.scraper.mapper;

import job.scraper.config.MapperConfig;
import job.scraper.dto.JobResponseDto;
import job.scraper.model.Job;
import job.scraper.model.Location;
import job.scraper.model.Tag;
import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class)
public interface JobMapper {
    @Mapping(target = "tags", source = "tags", qualifiedByName = "mapTags")
    @Mapping(target = "locations", source = "locations", qualifiedByName = "mapLocations")
    JobResponseDto toDto(Job job);

    @Named("mapTags")
    static List<String> mapTags(List<Tag> tags) {
        return tags.stream()
                .map(Tag::getName)
                .collect(Collectors.toList());
    }

    @Named("mapLocations")
    static List<String> mapLocations(List<Location> locations) {
        return locations.stream()
                .map(Location::getName)
                .collect(Collectors.toList());
    }
}
