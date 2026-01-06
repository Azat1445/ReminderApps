package org.example.reminderapp.mapper;


import org.example.reminderapp.dto.request.UserCreateDto;
import org.example.reminderapp.dto.response.UserProfileResponseDto;
import org.example.reminderapp.dto.request.UserProfileUpdateDto;
import org.example.reminderapp.entity.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {ReminderMapperDto.class})
public interface UserMapperDto {

    //Entity -> DTO

    UserProfileResponseDto toDto(User entity);

    //DTO -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "reminders", ignore = true)
    @Mapping(target = "telegramChatId", ignore = true)
    @Mapping(target = "notificationEnabled", ignore = true)
    User toEntity(UserCreateDto dto);

    //List mapping
    List<UserProfileResponseDto> toDtoList(List<User> entities);

    //Update entity of DTO

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "reminders", ignore = true)
    @Mapping(target = "telegramChatId", ignore = true)
    @Mapping(target = "notificationEnabled", ignore = true)
    void updateEntity(UserProfileUpdateDto dto, @MappingTarget User entity);

}
