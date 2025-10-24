CREATE DATABASE globalbooks_catalog;
CREATE DATABASE globalbooks_payments;
CREATE DATABASE globalbooks_shipping;

GRANT ALL PRIVILEGES ON DATABASE globalbooks_catalog TO postgres;
GRANT ALL PRIVILEGES ON DATABASE globalbooks_payments TO postgres;
GRANT ALL PRIVILEGES ON DATABASE globalbooks_shipping TO postgres;