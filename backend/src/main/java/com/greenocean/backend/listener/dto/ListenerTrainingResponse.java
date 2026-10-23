package com.greenocean.backend.listener.dto;

import java.util.List;

public record ListenerTrainingResponse(
        List<ListenerTrainingModuleResponse> modules, List<String> completedModules,
        int totalModules, boolean courseworkComplete,
        String applicationStatus, boolean listenerActivated
) {
}
