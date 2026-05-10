package com.agrotech.api.profile.application.usecase;

import com.agrotech.api.iam.domain.model.User;
import com.agrotech.api.iam.infrastructure.persistence.jpa.repository.UserRepository;
import com.agrotech.api.iam.domain.valueobject.UserRole;
import com.agrotech.api.iam.application.usecase.AuthenticatedUserService;
import com.agrotech.api.profile.infrastructure.web.dto.CreateProfileResource;
import com.agrotech.api.profile.infrastructure.web.dto.AdvisorCatalogResource;
import com.agrotech.api.profile.infrastructure.web.dto.UpdateProfileResource;
import com.agrotech.api.profile.application.mapper.ProfileMapper;
import com.agrotech.api.profile.domain.model.Advisor;
import com.agrotech.api.profile.domain.model.Farmer;
import com.agrotech.api.profile.domain.model.Profile;
import com.agrotech.api.profile.infrastructure.persistence.jpa.repository.AdvisorRepository;
import com.agrotech.api.profile.infrastructure.persistence.jpa.repository.FarmerRepository;
import com.agrotech.api.profile.infrastructure.persistence.jpa.repository.ProfileRepository;
import com.agrotech.api.shared.infrastructure.storage.GoogleStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Service
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final FarmerRepository farmerRepository;
    private final AdvisorRepository advisorRepository;
    private final UserRepository userRepository;
    private final AuthenticatedUserService authenticatedUserService;
    private final GoogleStorageService googleStorageService;
    private final ProfileMapper profileMapper;

    public ProfileService(
            ProfileRepository profileRepository,
            FarmerRepository farmerRepository,
            AdvisorRepository advisorRepository,
            UserRepository userRepository,
            AuthenticatedUserService authenticatedUserService,
            GoogleStorageService googleStorageService,
            ProfileMapper profileMapper
    ) {
        this.profileRepository = profileRepository;
        this.farmerRepository = farmerRepository;
        this.advisorRepository = advisorRepository;
        this.userRepository = userRepository;
        this.authenticatedUserService = authenticatedUserService;
        this.googleStorageService = googleStorageService;
        this.profileMapper = profileMapper;
    }

    public Profile getProfileById(Long id) {
        return requireProfile(id);
    }

    public Profile getCurrentProfile() {
        return getProfileEntityByUserId(authenticatedUserService.getCurrentUser().userId());
    }

    public List<Profile> getAdvisorProfiles() {
        return profileRepository.findByUser_Role(UserRole.ADVISOR);
    }

    public List<AdvisorCatalogResource> getAdvisorCatalog() {
        return advisorRepository.findAll().stream()
                .map(advisor -> profileMapper.toAdvisorCatalogResource(
                        advisor,
                        getProfileEntityByUserId(advisor.getUser().getId())
                ))
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public Profile createProfile(CreateProfileResource resource) throws IOException {
        Long userId = authenticatedUserService.getCurrentUser().userId();
        if (profileRepository.findByUser_Id(userId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Profile already exists for the current user");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Profile profile = profileMapper.toProfile(resource, user, uploadIfPresent(resource.photo()));
        return profileRepository.save(profile);
    }

    @Transactional(rollbackFor = Exception.class)
    public Profile updateProfile(Long id, UpdateProfileResource resource) throws IOException {
        Profile profile = requireProfile(id);
        profileMapper.updateProfile(profile, resource, uploadIfPresent(resource.photo()));
        return profileRepository.save(profile);
    }

    public void deleteProfile(Long id) {
        profileRepository.delete(requireProfile(id));
    }

    public Farmer getFarmerById(Long id) {
        return getFarmerEntity(id);
    }

    public Farmer getCurrentFarmer() {
        return requireCurrentFarmerEntity();
    }

    public void deleteFarmer(Long id) {
        farmerRepository.delete(getFarmerEntity(id));
    }

    public Advisor getAdvisorById(Long id) {
        return getAdvisorEntity(id);
    }

    public Advisor getAdvisorByUserId(Long userId) {
        return getAdvisorEntityByUserId(userId);
    }

    public Advisor getCurrentAdvisor() {
        return requireCurrentAdvisorEntity();
    }

    public void deleteAdvisor(Long id) {
        advisorRepository.delete(getAdvisorEntity(id));
    }

    public Farmer requireCurrentFarmerEntity() {
        Long farmerId = authenticatedUserService.requireRole(UserRole.FARMER).farmerId();
        return getFarmerEntity(farmerId);
    }

    public Advisor requireCurrentAdvisorEntity() {
        Long advisorId = authenticatedUserService.requireRole(UserRole.ADVISOR).advisorId();
        return getAdvisorEntity(advisorId);
    }

    public Farmer getFarmerEntity(Long id) {
        return farmerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Farmer not found"));
    }

    public Farmer getFarmerEntityByUserId(Long userId) {
        return farmerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Farmer not found"));
    }

    public Advisor getAdvisorEntity(Long id) {
        return advisorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Advisor not found"));
    }

    public Advisor getAdvisorEntityByUserId(Long userId) {
        return advisorRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Advisor not found"));
    }

    public Profile getProfileEntityByUserId(Long userId) {
        return profileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
    }

    public Profile getProfileEntityByFarmerId(Long farmerId) {
        Farmer farmer = getFarmerEntity(farmerId);
        return getProfileEntityByUserId(farmer.getUser().getId());
    }

    public Profile getProfileEntityByAdvisorId(Long advisorId) {
        Advisor advisor = getAdvisorEntity(advisorId);
        return getProfileEntityByUserId(advisor.getUser().getId());
    }

    public void updateAdvisorRating(Long advisorId, BigDecimal rating) {
        Advisor advisor = getAdvisorEntity(advisorId);
        advisor.setRating(rating);
        advisorRepository.save(advisor);
    }

    private Profile requireProfile(Long id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
    }

    private String uploadIfPresent(org.springframework.web.multipart.MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        return googleStorageService.uploadFile(file);
    }
}
