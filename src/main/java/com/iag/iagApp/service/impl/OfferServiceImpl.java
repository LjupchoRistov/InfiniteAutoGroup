package com.iag.iagApp.service.impl;

import com.iag.iagApp.dto.OfferDto;
import com.iag.iagApp.exceptions.InvalidOfferIdException;
import com.iag.iagApp.mapper.OfferMapper;
import com.iag.iagApp.model.MakeEntity;
import com.iag.iagApp.model.ModelEntity;
import com.iag.iagApp.model.Offer;
import com.iag.iagApp.model.UserEntity;
import com.iag.iagApp.model.enums.Style;
import com.iag.iagApp.repository.MakeRepository;
import com.iag.iagApp.repository.ModelRepository;
import com.iag.iagApp.repository.OfferRepository;
import com.iag.iagApp.repository.UserRepository;
import com.iag.iagApp.service.OfferService;
import org.springframework.stereotype.Service;

import javax.sound.midi.Soundbank;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

//todo: implement mappers
import static com.iag.iagApp.mapper.OfferMapper.*;

@Service
public class OfferServiceImpl implements OfferService {

    private final OfferRepository offerRepository;
    private final UserRepository userRepository;
    private final MakeRepository makeRepository;
    private final ModelRepository modelRepository;

    public OfferServiceImpl(OfferRepository offerRepository, UserRepository userRepository, MakeRepository makeRepository, ModelRepository modelRepository) {
        this.offerRepository = offerRepository;
        this.userRepository = userRepository;
        this.makeRepository = makeRepository;
        this.modelRepository = modelRepository;
    }

    @Override
    public OfferDto findById(Long id){
        return mapToOfferDto(this.offerRepository.findById(id).get());
    }

    @Override
    public OfferDto findByTitle(String title) {
        return mapToOfferDto(this.offerRepository.findByTitle(title));
    }

    @Override
    public List<OfferDto> findAllWithSameMakeAndModel(OfferDto offerDto) {
        MakeEntity make = makeRepository.findByMakeNameEquals(offerDto.getMake());
        ModelEntity model = modelRepository.findByModelNameEqualsAndMakeEquals(offerDto.getModel(), make);

        List<Offer> offerList = this.offerRepository.findAllByModelEquals(model);

        //todo: shuffle the list
        Collections.shuffle(offerList);
        //todo: remove the post from its own suggestions
        offerList.removeIf(offer -> offer.getId().equals(offerDto.getId()));

        return offerList.stream().map(OfferMapper::mapToOfferDto).limit(8).toList();
    }

    @Override
    public List<OfferDto> findAllWithSameMake(OfferDto offerDto) {
        MakeEntity make = makeRepository.findByMakeNameEquals(offerDto.getMake());

        // Get all Offers that contain make
        List<Offer> offers = this.offerRepository.findAll();
        offers.removeIf(offer -> !offer.getModel().getMake().equals(make));

        // Shuffle the list
        Collections.shuffle(offers);

        // Remove the post from its own suggestions
        offers.removeIf(offer -> offer.getId().equals(offerDto.getId()));

        return offers.stream().map(OfferMapper::mapToOfferDto).limit(8).toList();
    }

    @Override
    public List<OfferDto> findAllWithSameStyle(OfferDto offerDto) {
        Style style = offerDto.getStyle();

        List<Offer> offerList = this.offerRepository.findAllByStyleEquals(style);

        // Shuffle the list
        Collections.shuffle(offerList);
        // Remove the post from its own suggestions
        offerList.removeIf(offer -> offer.getId().equals(offerDto.getId()));

        return offerList.stream().map(OfferMapper::mapToOfferDto).limit(8).toList();
    }

    @Override
    public List<OfferDto> filterOffersCategories(String filterType, String filterAttr) {
        List<Offer> offerList = this.offerRepository.findAll();
        if (filterType.equals("fuel"))
            offerList.removeIf(offer -> !offer.getFuel().name().equalsIgnoreCase(filterAttr));
        else if (filterType.equals("engineLiters"))
            offerList.removeIf(offer -> !offer.getEngineLiters().equalsIgnoreCase(filterAttr));
        else if (filterType.equals("engineType"))
            offerList.removeIf(offer -> !offer.getEngineType().name().equalsIgnoreCase(filterAttr));
        else if (filterType.equals("engineCylinders"))
            offerList.removeIf(offer -> !offer.getEngineCylinders().toString().equals(filterAttr));
        else if (filterType.equals("enginePower"))
            offerList.removeIf(offer -> !offer.getEnginePower().toString().equalsIgnoreCase(filterAttr));
        else if (filterType.equals("style"))
            offerList.removeIf(offer -> !offer.getStyle().name().equalsIgnoreCase(filterAttr));

        if (offerList.isEmpty())
            offerList = this.offerRepository.findAll();

        return offerList.stream().map(OfferMapper::mapToOfferDto).collect(Collectors.toList());
    }

    @Override
    public List<OfferDto> findAllOffers() {
        List<Offer> offers = this.offerRepository.findAll();
        return offers.stream().map(OfferMapper::mapToOfferDto).toList();
    }

    @Override
    public void saveOffer(OfferDto offerDto) {
//        String username = SecurityUtil.getSessionUser();
        UserEntity user = userRepository.findAll().getFirst();
        offerDto.setPictures(getImages());
        offerDto.setCoverPicture(getImages().getFirst());
        Offer offer = mapToOffer(offerDto, modelRepository, makeRepository);

        offer.setCreatedBy(user);
        offer.setCoverPicture(offerDto.getCoverPicture());

        this.offerRepository.save(offer);
    }

    @Override
    public void updateOffer(OfferDto offerDto) {
        Offer previous = this.offerRepository.findById(offerDto.getId()).get();
        offerDto.setCoverPicture(previous.getCoverPicture());
        offerDto.setPictures(previous.getPictures());
        offerDto.setCreatedBy(previous.getCreatedBy());
        offerDto.setCreatedOn(previous.getCreatedOn());

        Offer newOffer = mapToOffer(offerDto, modelRepository, makeRepository);

        this.offerRepository.save(newOffer);
    }

    @Override
    public void deleteOffer(Long id) {
        this.offerRepository.deleteById(id);
    }

    // todo: NEEDS TO BE REMOVED
    private List<String> getImages(){
        List<String> images = new ArrayList<>();
        images.add("https://upload.wikimedia.org/wikipedia/commons/thumb/a/ac/No_image_available.svg/2048px-No_image_available.svg.png");
        return images;
    }
}
