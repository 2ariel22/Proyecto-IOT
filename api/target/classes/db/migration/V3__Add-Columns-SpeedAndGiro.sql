ALTER TABLE components
ADD speed INT;

ALTER TABLE components
ADD giro INT;

UPDATE components
SET speed = 100, giro = 1;