CREATE TABLE IF NOT EXISTS products (
    product_id VARCHAR(50) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    isbn VARCHAR(20) UNIQUE NOT NULL,
    description TEXT,
    category VARCHAR(100) NOT NULL,
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    currency VARCHAR(3) DEFAULT 'USD',
    stock_quantity INTEGER NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    reserved_quantity INTEGER NOT NULL DEFAULT 0 CHECK (reserved_quantity >= 0),
    warehouse_location VARCHAR(100),
    publish_date DATE,
    image_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    restock_date DATE,
    CONSTRAINT check_reserved CHECK (reserved_quantity <= stock_quantity)
);

-- Create indexes for better performance
CREATE INDEX idx_products_title ON products(LOWER(title));
CREATE INDEX idx_products_author ON products(LOWER(author));
CREATE INDEX idx_products_category ON products(LOWER(category));
CREATE INDEX idx_products_price ON products(price);
CREATE INDEX idx_products_stock ON products(stock_quantity);

-- Create trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_products_updated_at
BEFORE UPDATE ON products
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Insert sample data
INSERT INTO products (product_id, title, author, isbn, description, category, price, stock_quantity, warehouse_location, publish_date) VALUES
('BOOK-001', 'Effective Java', 'Joshua Bloch', '978-0134685991', 'The definitive guide to Java best practices', 'Programming', 45.99, 150, 'Warehouse-A', '2018-01-06'),
('BOOK-002', 'Clean Code', 'Robert C. Martin', '978-0132350884', 'A handbook of agile software craftsmanship', 'Programming', 39.99, 200, 'Warehouse-A', '2008-08-01'),
('BOOK-003', 'Design Patterns', 'Gang of Four', '978-0201633610', 'Elements of reusable object-oriented software', 'Programming', 54.99, 75, 'Warehouse-B', '1994-10-31'),
('BOOK-004', 'The Pragmatic Programmer', 'David Thomas', '978-0135957059', 'Your journey to mastery', 'Programming', 49.99, 120, 'Warehouse-A', '2019-09-13'),
('BOOK-005', 'Introduction to Algorithms', 'Thomas H. Cormen', '978-0262033848', 'Comprehensive textbook on algorithms', 'Computer Science', 89.99, 50, 'Warehouse-C', '2009-07-31'),
('BOOK-006', 'The Mythical Man-Month', 'Frederick Brooks', '978-0201835953', 'Essays on software engineering', 'Software Engineering', 29.99, 180, 'Warehouse-B', '1995-08-02'),
('BOOK-007', 'Domain-Driven Design', 'Eric Evans', '978-0321125217', 'Tackling complexity in software', 'Architecture', 65.99, 90, 'Warehouse-C', '2003-08-30'),
('BOOK-008', 'Microservices Patterns', 'Chris Richardson', '978-1617294549', 'With examples using Java and Spring Boot', 'Architecture', 49.99, 110, 'Warehouse-A', '2018-10-27'),
('BOOK-009', 'Spring in Action', 'Craig Walls', '978-1617297571', 'Covers Spring 5 and Spring Boot 2', 'Framework', 44.99, 135, 'Warehouse-B', '2020-05-01'),
('BOOK-010', 'Kubernetes in Action', 'Marko Luksa', '978-1617293726', 'Learn Kubernetes from the ground up', 'DevOps', 59.99, 95, 'Warehouse-C', '2017-12-01');

-- Grant permissions to catalog_user (run as superuser)
-- GRANT ALL PRIVILEGES ON TABLE products TO catalog_user;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO catalog_user;