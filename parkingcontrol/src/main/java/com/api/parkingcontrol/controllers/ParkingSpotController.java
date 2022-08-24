package com.api.parkingcontrol.controllers;

import com.api.parkingcontrol.dtos.ParkingSpotDto;
import com.api.parkingcontrol.models.ParkinSpotModel;
import com.api.parkingcontrol.services.ParkingSpotService;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/parking-spot")
public class ParkingSpotController {

    final ParkingSpotService parkingSpotService;

    public ParkingSpotController(ParkingSpotService parkingSpotService) {
        this.parkingSpotService = parkingSpotService;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "Efetua o cadastro de uma vaga de garagem")
    @PostMapping(produces="application/json", consumes="application/json")
    public ResponseEntity<Object> saveparkingSpot(@RequestBody @Valid ParkingSpotDto parkingSpotDto ){

        if(parkingSpotService.existsByLicensePlateCar(parkingSpotDto.getLicensePlateCar())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: License Plate Car is already in use!");
        }
        if(parkingSpotService.existsByParkingSpotNumber(parkingSpotDto.getParkingSpotNumber())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Parking Spot is already in use!");
        }
        if(parkingSpotService.existsByApartmentAndBlock(parkingSpotDto.getApartment(), parkingSpotDto.getBlock())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Parking Spot already registered for this apartment/block!");
        }

        var parkingSpotModel = new ParkinSpotModel();
        BeanUtils.copyProperties(parkingSpotDto, parkingSpotModel);
        parkingSpotModel.setRegistrationDate(LocalDateTime.now(ZoneId.of("UTC")));
        return ResponseEntity.status(HttpStatus.CREATED).body(parkingSpotService.save(parkingSpotModel));
    }
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @ApiOperation(value = "Lista todos os registros")
    @GetMapping( produces="application/json")
    public ResponseEntity<List<ParkinSpotModel>> getAllParkingSpots(){

        //FORMA DE UTILIZAÇÃO COM HATEOS PARA SE ADEQUAR AO PADRAO RESTFULL, ESSA DEPENDENCIA CRIA LINKS
        List<ParkinSpotModel> parkinSpotModelsList = parkingSpotService.findAll();
        if(parkinSpotModelsList.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }else{
            for (ParkinSpotModel parkinSpotModels : parkinSpotModelsList){
                UUID id = parkinSpotModels.getId();
                parkinSpotModels.add(linkTo(methodOn(ParkingSpotController.class).getOneParkingSpot(id)).withSelfRel());
            }
            return new ResponseEntity< List<ParkinSpotModel>>(parkinSpotModelsList, HttpStatus.OK);
        }

        /*    abaixo é uma forma de utilizar sem o HATEOAS
        return  ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.findAll());

         */
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @ApiOperation(value = "Inseri paginação da listagem dos registros")
    @GetMapping(value="/paginacaoList",  produces="application/json")
    public ResponseEntity<Page<ParkinSpotModel>> getAllParkingSpotspagination(@PageableDefault(page=0, size=10, sort="id", direction= Sort.Direction.ASC) Pageable pageable){
        return  ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.findAllPagination(pageable));
    }
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @ApiOperation(value = "Lista um registro em especifico")
    @GetMapping(value="/{id}",  produces="application/json")
    public ResponseEntity<Object> getOneParkingSpot(@PathVariable(value = "id") UUID id){
        Optional<ParkinSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);
        if(!parkingSpotModelOptional.isPresent()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found!");
        }else{
            parkingSpotModelOptional.get().add(linkTo(methodOn(ParkingSpotController.class).getAllParkingSpots()).withRel("Lista de vagas"));
        }
        return ResponseEntity.status(HttpStatus.OK).body(parkingSpotModelOptional.get());
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "Exclui do banco um determinado registro")
    @DeleteMapping(value = "/{id}", produces="application/json", consumes="application/json")
    public ResponseEntity<Object> deleteParkingSpot(@PathVariable(value = "id") UUID id){
        Optional<ParkinSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);
        if(!parkingSpotModelOptional.isPresent()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found!");
        }

        parkingSpotService.delete(parkingSpotModelOptional.get());
        return ResponseEntity.status(HttpStatus.OK).body("parking Spot deleted successfully");
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "Efetua alteração em um determinado registro")
    @PutMapping(value = "/{id}", produces="application/json", consumes="application/json")
    ResponseEntity<Object> updateParkingSpot(@PathVariable(value = "id") UUID id, @RequestBody @Valid ParkingSpotDto parkingSpotDto){
        Optional<ParkinSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);
        if(!parkingSpotModelOptional.isPresent()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found!");
        }
        var parkingSpotModel = new ParkinSpotModel();
        BeanUtils.copyProperties(parkingSpotDto, parkingSpotModel);
        parkingSpotModel.setId(parkingSpotModelOptional.get().getId());
        parkingSpotModel.setRegistrationDate(parkingSpotModelOptional.get().getRegistrationDate());

        return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.save(parkingSpotModel));
    }




}
