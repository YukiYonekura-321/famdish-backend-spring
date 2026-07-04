package com.example.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.dto.ContactAttributesRequest;
import com.example.backend.dto.MessageResponse;
import com.example.backend.entity.Contact;
import com.example.backend.exception.ValidationException;
import com.example.backend.repository.ContactRepository;

@Service
public class ContactService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final ContactRepository contactRepository;

    public ContactService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @Transactional
    public MessageResponse create(ContactAttributesRequest attrs) {
        List<String> errors = validate(attrs);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        Contact contact = new Contact();
        contact.setName(attrs.name());
        contact.setEmail(attrs.email());
        contact.setSubject(attrs.subject());
        contact.setMessage(attrs.message());
        contactRepository.save(contact);

        return new MessageResponse("お問い合わせを受け付けました");
    }

    private List<String> validate(ContactAttributesRequest attrs) {
        List<String> errors = new ArrayList<>();
        if (attrs == null) {
            errors.add("Name can't be blank");
            errors.add("Email can't be blank");
            errors.add("Subject can't be blank");
            errors.add("Message can't be blank");
            return errors;
        }
        if (isBlank(attrs.name())) {
            errors.add("Name can't be blank");
        }
        if (isBlank(attrs.email())) {
            errors.add("Email can't be blank");
        } else if (!EMAIL_PATTERN.matcher(attrs.email()).matches()) {
            errors.add("Email is invalid");
        }
        if (isBlank(attrs.subject())) {
            errors.add("Subject can't be blank");
        }
        if (isBlank(attrs.message())) {
            errors.add("Message can't be blank");
        }
        return errors;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

