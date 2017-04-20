package ml.matteolobello.quickphoto.pojo;

import android.net.Uri;

public class Photo {

    private final String mName;
    private final Uri mUri;

    public Photo(Uri uri) {
        this(null, uri);
    }

    public Photo(String name, Uri uri) {
        mName = name;
        mUri = uri;
    }

    public Uri getUri() {
        return mUri;
    }

    public String getName() {
        return mName;
    }
}
