package naiveversion2.common;

import aic2021.user.Location;
import aic2021.user.Resource;

public class ResourceTarget extends Object {
    public Resource resource;
    public Location location;

    public ResourceTarget(Resource resource, Location location) {
        this.resource = resource;
        this.location = location;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof ResourceTarget)) {
            return false;
        }

        ResourceTarget r = (ResourceTarget) o;
        return this.location.equals(r.location);
    }
}
