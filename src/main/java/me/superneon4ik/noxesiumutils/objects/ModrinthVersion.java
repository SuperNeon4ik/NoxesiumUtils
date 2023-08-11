package me.superneon4ik.noxesiumutils.objects;

import java.time.Instant;

public class ModrinthVersion {
    public String id;
    public String project_id;
    public String author_id;
    public boolean featured;
    public String name;
    public String version_number;
    public String date_published;
    public String version_type;

    public ModrinthVersion() {}

    public long datePublishedNumericTimestamp() {
        var instant = Instant.parse(date_published);
        return instant.getEpochSecond();
    }

    public int compareDatePublishedTo(ModrinthVersion other) {
        return Long.compare(other.datePublishedNumericTimestamp(), this.datePublishedNumericTimestamp());
    }
}
