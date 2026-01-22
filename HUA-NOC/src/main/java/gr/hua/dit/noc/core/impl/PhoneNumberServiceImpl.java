package gr.hua.dit.noc.core.impl;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import gr.hua.dit.noc.core.PhoneNumberService;
import gr.hua.dit.noc.core.model.PhoneNumberValidationResult;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * libphonenumber-based implementation of {@link PhoneNumberService}.
 * Enforces STRICT validation for Greek numbers (+30).
 *
 * @author Dimitris Gkoulis
 */
@Service
public class PhoneNumberServiceImpl implements PhoneNumberService {

    private final PhoneNumberUtil phoneNumberUtil;
    private final String defaultRegion;

    public PhoneNumberServiceImpl() {
        this.phoneNumberUtil = PhoneNumberUtil.getInstance();
        // Ορίζουμε το Default Region σε Ελλάδα, ώστε το "69..." να αναγνωρίζεται ως "+3069..."
        this.defaultRegion = "GR";
    }

    @Override
    public PhoneNumberValidationResult validatePhoneNumber(final String rawPhoneNumber) {
        // 1. Βασικοί έλεγχοι για κενά
        if (rawPhoneNumber == null || rawPhoneNumber.isBlank()) {
            return PhoneNumberValidationResult.invalid(rawPhoneNumber);
        }

        try {
            // 2. Προσπάθεια Parsing (Αναγνώριση αριθμού)
            final Phonenumber.PhoneNumber phoneNumber = this.phoneNumberUtil.parse(rawPhoneNumber, this.defaultRegion);

            // 3. Ελεγχος Εγκυρότητας (isValidNumber)
            // Αυτό διορθώνει το bug με το "12345". Η libphonenumber τώρα ελέγχει μήκος και prefix.
            if (!this.phoneNumberUtil.isValidNumber(phoneNumber)) {
                return PhoneNumberValidationResult.invalid(rawPhoneNumber);
            }

            // 4. Έλεγχος Χώρας (Μόνο Ελλάδα - +30)
            // Αν ο αριθμός είναι έγκυρος αλλά είναι π.χ. Αγγλικός (+44...), τον απορρίπτουμε.
            if (phoneNumber.getCountryCode() != 30) {
                return PhoneNumberValidationResult.invalid(rawPhoneNumber);
            }

            // 5. Αν όλα είναι σωστά, επιστροφή Valid αποτελέσματος
            // Επιστρέφουμε τον αριθμό σε μορφή E.164 (π.χ. +306971234567)
            return PhoneNumberValidationResult.valid(
                    rawPhoneNumber,
                    this.phoneNumberUtil.getNumberType(phoneNumber).name().toLowerCase(Locale.ROOT),
                    this.phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164)
            );

        } catch (NumberParseException e) {
            // Αν δεν μπορεί καν να γίνει parse (π.χ. γράμματα αντί για αριθμούς)
            return PhoneNumberValidationResult.invalid(rawPhoneNumber);
        }
    }
}