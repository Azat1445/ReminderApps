package org.example.reminderapp.mapper;

import org.example.reminderapp.dto.request.ReminderCreateDto;
import org.example.reminderapp.dto.request.ReminderUpdateDto;
import org.example.reminderapp.dto.response.ReminderResponseDto;
import org.example.reminderapp.entity.Reminder;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ReminderMapperDto {

    //Entity -> DTO
//    @Mapping(target = "remindAt", source = "remindAt")
    @Mapping(target = "userId", ignore = true)
    ReminderResponseDto toDto(Reminder entity);

    //DTO -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "user", ignore = true)
//    @Mapping(target = "remindAt", source = "remindAt")
    Reminder toEntity(ReminderCreateDto dto);

    //List mapping
    List<ReminderResponseDto> toDtoList(List<Reminder> entities);

    //Update Entity of DTO
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "user", ignore = true)
//    @Mapping(target = "remindAt", source = "remindAt")
    void updateEntity(ReminderUpdateDto dto, @MappingTarget Reminder entity);
}
