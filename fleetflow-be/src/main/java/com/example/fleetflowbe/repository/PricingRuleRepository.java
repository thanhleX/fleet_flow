package com.example.fleetflowbe.repository;

import com.example.fleetflowbe.common.constants.RegionZone;
import com.example.fleetflowbe.common.constants.ServiceTier;
import com.example.fleetflowbe.entity.PricingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {

    Optional<PricingRule> findByRegionZoneAndServiceTier(RegionZone regionZone, ServiceTier serviceTier);
}

