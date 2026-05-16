package org.example.workhub.domain.mapper;

import org.example.workhub.domain.dto.response.SubscriberResponse;
import org.example.workhub.domain.entity.Skill;
import org.example.workhub.domain.entity.Subscriber;
import org.example.workhub.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SubscriberMapper {

    default SubscriberResponse toResponse(Subscriber subscriber) {
        if (subscriber == null) {
            return null;
        }
        return SubscriberResponse.builder()
                .id(subscriber.getId())
                .name(subscriber.getName())
                .email(subscriber.getEmail())
                .enabled(subscriber.getEnabled())
                .deleted(subscriber.getDeleted())
                .subscribedAt(subscriber.getSubscribedAt())
                .lastEmailSentAt(subscriber.getLastEmailSentAt())
                .user(mapUser(subscriber.getUser()))
                .skills(mapSkills(subscriber.getSkills()))
                .createdDate(subscriber.getCreatedDate())
                .lastModifiedDate(subscriber.getLastModifiedDate())
                .build();
    }

    List<SubscriberResponse> toResponses(List<Subscriber> subscribers);

    default SubscriberResponse.UserInfo mapUser(User user) {
        if (user == null) {
            return null;
        }
        return SubscriberResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    default List<SubscriberResponse.SkillInfo> mapSkills(List<Skill> skills) {
        if (skills == null) {
            return null;
        }
        return skills.stream()
                .map(skill -> SubscriberResponse.SkillInfo.builder()
                        .id(skill.getId())
                        .name(skill.getName())
                        .level(skill.getLevel())
                        .build())
                .collect(Collectors.toList());
    }
}
