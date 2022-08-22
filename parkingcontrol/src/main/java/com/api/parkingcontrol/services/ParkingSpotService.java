package com.api.parkingcontrol.services;

import com.api.parkingcontrol.models.ParkinSpotModel;
import com.api.parkingcontrol.repositories.ParkinSpotRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ParkingSpotService {

    final ParkinSpotRepository parkinSpotRepository;

    public ParkingSpotService(ParkinSpotRepository parkinSpotRepository) {
        this.parkinSpotRepository = parkinSpotRepository;
    }

    @Transactional
    public ParkinSpotModel save(ParkinSpotModel parkingSpotModel) {
        return parkinSpotRepository.save(parkingSpotModel);
    }

    public boolean existsByLicensePlateCar(String licensePlateCar) {
        return parkinSpotRepository.existsByLicensePlateCar(licensePlateCar);
    }

    public boolean existsByParkingSpotNumber(String parkingSpotNumber) {
        return  parkinSpotRepository.existsByParkingSpotNumber(parkingSpotNumber);
    }

    public boolean existsByApartmentAndBlock(String apartment, String block) {
        return parkinSpotRepository.existsByApartmentAndBlock(apartment, block);

    }

    public List<ParkinSpotModel> findAll() {
        return parkinSpotRepository.findAll();
    }

    public Optional<ParkinSpotModel> findById(UUID id) {
        return parkinSpotRepository.findById(id);
    }

    @Transactional
    public void delete(ParkinSpotModel parkinSpotModel) {
        parkinSpotRepository.delete(parkinSpotModel);
    }


    public Page<ParkinSpotModel> findAllPagination(Pageable pageable) {
        return parkinSpotRepository.findAll(pageable);
    }
}