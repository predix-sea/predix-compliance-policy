package com.predix.compliance.geo;

import java.util.Optional;

public interface GeoProvider {
    Optional<GeoResolution> resolve(String ip);
}
