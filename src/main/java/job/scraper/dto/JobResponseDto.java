package job.scraper.dto;

import java.util.List;
import lombok.Data;

@Data
public class JobResponseDto {
    private Long id;
    private String jobPageUrl;
    private String positionName;
    private String organizationUrl;
    private String logoUrl;
    private String organizationTitle;
    private String laborFunction;
    private Long postedDateUnix;
    private String descriptionHtml;
    private List<String> tags;
    private List<String> locations;
}
