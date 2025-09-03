CREATE TABLE IF NOT EXISTS `developers` (
  `id` CHAR(36) NOT NULL, -- UUID string (36 characters with hyphens)
  `keycloak_user_id` VARCHAR(255) NOT NULL,
  `name` VARCHAR(150) NOT NULL,
  `email` VARCHAR(255) NOT NULL,
  `website` VARCHAR(255) NULL,
  `company_name` VARCHAR(150) NULL,
  `bio` TEXT NULL,
  `logo_url` VARCHAR(255) NULL,
  `location` VARCHAR(100) NULL,
  `status` VARCHAR(30) NOT NULL,
  `developer_type` VARCHAR(20) NOT NULL,
  `is_verified` BOOLEAN NOT NULL DEFAULT FALSE,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_developer_keycloak_id` (`keycloak_user_id`),
  UNIQUE KEY `uk_developer_email` (`email`)
);
