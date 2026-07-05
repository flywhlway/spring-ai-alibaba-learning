package com.flywhl.saa.office.service;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.flywhl.saa.office.model.entity.AssistantArtifact;
import com.flywhl.saa.office.model.entity.SysUser;
import com.flywhl.saa.office.repository.AssistantArtifactRepository;

@Service
public class ArtifactService {
    private final AssistantArtifactRepository artifactRepository;
    public ArtifactService(AssistantArtifactRepository artifactRepository) {
        this.artifactRepository = artifactRepository;
    }
    @Transactional
    public AssistantArtifact save(SysUser user, String type, String title, String content, String filePath, String model) {
        AssistantArtifact artifact = new AssistantArtifact();
        artifact.setUserId(user.getId());
        artifact.setType(type);
        artifact.setTitle(title);
        artifact.setContent(content);
        artifact.setFilePath(filePath);
        artifact.setModel(model);
        artifact.setCreatedAt(LocalDateTime.now());
        return artifactRepository.save(artifact);
    }
}

