
package com.labmanagement.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.labmanagement.entity.Referral;

public interface ReferralDAO extends JpaRepository<Referral, Integer> {

	Optional<Referral> findByReferralCode(String referralCode);

	Optional<Referral> findByVisitId(Integer visitId);

	List<Referral> findByActiveFlagOrderByReferralDateDesc(Integer activeFlag);

	boolean existsByReferralCode(String referralCode);

	long countByVisit_Doctor_DoctorCodeAndActiveFlag(String doctorCode, Integer activeFlag);

	List<Referral> findByActiveFlagAndVisit_Doctor_DoctorCodeOrderByReferralDateDesc(Integer activeFlag,
			String doctorCode);
}
