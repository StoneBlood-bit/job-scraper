package job.scraper.util;

public class JsScripts {
    public static final String SCRIPT_SCROLL_INTO_VIEW = "arguments[0].scrollIntoView(true);";
    public static final String SCRIPT_SCROLL_TOP = "arguments[0].scrollTop += 300;";
    public static final String SCRIPT_CLICK = "arguments[0].click();";
    public static final String SCRIPT_SCROLL_WINDOW = "window.scrollTo(0, document.body.scrollHeight - 700);";

}
