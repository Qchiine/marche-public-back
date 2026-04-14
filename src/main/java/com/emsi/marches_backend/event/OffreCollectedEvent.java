package com.emsi.marches_backend.event;

import com.emsi.marches_backend.model.OffreMarcheDocument;

import java.util.List;

public record OffreCollectedEvent(List<OffreMarcheDocument> offresCollectees) {
    public OffreCollectedEvent {
        offresCollectees = offresCollectees == null ? List.of() : List.copyOf(offresCollectees);
    }
}
