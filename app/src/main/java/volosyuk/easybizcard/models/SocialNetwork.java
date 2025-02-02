package volosyuk.easybizcard.models;

public class SocialNetwork {
    private final String name;
    private final int iconResId;
    private final String urlTemplate;
    private String link;

    public SocialNetwork(String name, int iconResId, String urlTemplate) {
        this.name = name;
        this.iconResId = iconResId;
        this.urlTemplate = urlTemplate;
    }

    public String getName() {
        return name;
    }

    public int getIconResId() {
        return iconResId;
    }

    public String getUrlTemplate() {
        return urlTemplate;
    }

    public void setLink(String link){
        this.link = link;
    }

    public String getLink(){
        return link;
    }

    @Override
    public String toString() {
        return "SocialNetwork{name='" + name + "', link='" + link + "'}";
    }
}
