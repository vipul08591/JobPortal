package com.jobportal.jobportal.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.jobportal.jobportal.Entity.CandidateProfile;
import com.jobportal.jobportal.Entity.User;
import com.jobportal.jobportal.Repository.CandidateProfileRepository;

@Service
public class CandidateProfileService {

    private final String RESUME_DIR = "uploads/resumes/";
    private final String PROFILE_PIC_DIR = "uploads/profiles/";

    private final CandidateProfileRepository profileRepo;

    @Autowired
    public CandidateProfileService(CandidateProfileRepository profileRepo) {
        this.profileRepo = profileRepo;
    }

    // Get existing profile or create one for an existing user
    public CandidateProfile getProfileByUser(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User must exist in DB before creating a profile");
        }
        return profileRepo.findByUser(user)
            .orElseGet(() -> {
                CandidateProfile profile = new CandidateProfile();
                profile.setUser(user);
                return profile; // do not save yet
            });
    }

    // Save profile
    public CandidateProfile saveProfile(CandidateProfile profile) {
        if (profile.getUser() == null || profile.getUser().getId() == null) {
            throw new IllegalArgumentException("Profile must be linked to an existing user");
        }
        return profileRepo.save(profile);
    }

    // Save resume file
    public void saveResume(CandidateProfile profile, MultipartFile file) throws IOException {
        if (profile.getUser() == null || profile.getUser().getId() == null) {
            throw new IllegalArgumentException("Profile must be linked to an existing user");
        }

        File dir = new File(RESUME_DIR);
        if (!dir.exists()) dir.mkdirs();

        // Delete old resume if exists
        if (profile.getProfileResumeFile() != null) {
            Path oldPath = Paths.get(RESUME_DIR + profile.getProfileResumeFile());
            if (Files.exists(oldPath)) Files.delete(oldPath);
        }

        String filename = profile.getUser().getId() + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path path = Paths.get(RESUME_DIR + filename);
        Files.write(path, file.getBytes());

        profile.setProfileResumeFile(filename);
        profileRepo.save(profile);
    }

    // Save profile picture
    public void saveProfilePicture(CandidateProfile profile, MultipartFile file) throws IOException {
        if (profile.getUser() == null || profile.getUser().getId() == null) {
            throw new IllegalArgumentException("Profile must be linked to an existing user");
        }

        File dir = new File(PROFILE_PIC_DIR);
        if (!dir.exists()) dir.mkdirs();

        // Delete old picture if exists
        if (profile.getProfilePicture() != null) {
            Path oldPath = Paths.get(PROFILE_PIC_DIR + profile.getProfilePicture());
            if (Files.exists(oldPath)) Files.delete(oldPath);
        }

        String filename = profile.getUser().getId() + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path path = Paths.get(PROFILE_PIC_DIR + filename);
        Files.write(path, file.getBytes());

        profile.setProfilePicture(filename);
        profileRepo.save(profile);
    }
}
