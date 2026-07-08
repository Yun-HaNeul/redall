package io.security.redall.center.repository;

import io.security.redall.center.domain.BloodCenter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BloodCenterRepository extends JpaRepository<BloodCenter, Long> {

    Optional<BloodCenter> findByNameAndAddress(String name, String address);

    List<BloodCenter> findByLatIsNotNullAndLonIsNotNull();

    List<BloodCenter> findByNameContaining(String keyword);
}
