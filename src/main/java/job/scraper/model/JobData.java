package job.scraper.model;

import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobData {
    String url;
    List<String> tags;

    public JobData(String url, List<String> tags) {
        this.url = url;
        this.tags = tags;
    }
}
