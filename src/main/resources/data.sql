INSERT INTO users (id, name, role, active) VALUES ('kwame', 'Kwame Mensah', 'FINANCE', true);

INSERT INTO loans (id, status, amount) VALUES ('L1', 'INITIATED', 50000.00);

INSERT INTO disbursements (id, status, amount, loan_id) VALUES ('D1', 'PENDING', 20000.00, 'L1');
INSERT INTO disbursements (id, status, amount, loan_id) VALUES ('D2', 'PENDING', 30000.00, 'L1');
