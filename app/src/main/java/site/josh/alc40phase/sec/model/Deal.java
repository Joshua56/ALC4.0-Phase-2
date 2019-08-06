package site.josh.alc40phase.sec.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@IgnoreExtraProperties
public class Deal implements Serializable {

    public static String COLLECTION = "deals";

    public String uuid;
    public String name;
    public String amount;
    public String description;
    public String imageUri;
    public String uid; // userid

    /** no-args required*/
    public Deal() {
    }

    public Deal(String name, String amount, String description, String imageUri, String uid) {
        this.uuid = UUID.randomUUID().toString();
        this.name = name;
        this.amount = amount;
        this.description = description;
        this.imageUri = imageUri;
        this.uid = uid;
    }

    public String getUuid() {
        return this.uuid;
    }

    @Exclude
    public Map<String, Object> toMap() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("uuid", uuid);
        map.put("name", name);
        map.put("amount", amount);
        map.put("description", description);
        map.put("imageUri", imageUri);
        map.put("uid", uid);

        return map;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Deal deal = (Deal) o;
        return Objects.equals(uuid, deal.uuid) &&
                Objects.equals(name, deal.name) &&
                Objects.equals(amount, deal.amount) &&
                Objects.equals(description, deal.description) &&
                Objects.equals(imageUri, deal.imageUri) &&
                Objects.equals(uid, deal.uid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, name, amount, description, imageUri, uid);
    }
}
