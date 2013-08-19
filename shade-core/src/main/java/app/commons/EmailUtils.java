/*
 * Copyright (C) 2013 Pascal Mazars
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package app.commons;

import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 *
 */
public class EmailUtils {
    public static final String FROM_EMAIL_ADRRESS = "pma@decisive.no";
    public static final String TO_EMAIL_ADRRESS = "pma@decisive.no";

    private static JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

    static {
        mailSender.setHost(ConfigurationUtils.getString("email.host"));
    }

    public static void send(String subject, String text) {
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(FROM_EMAIL_ADRRESS);
			message.setTo(TO_EMAIL_ADRRESS);
			message.setSubject(subject);
			message.setText(text);

			mailSender.send(message);
		} catch (MailSendException e) {
		} catch (MailException e) {
		}
	}
}
