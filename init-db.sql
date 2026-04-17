-- Create database if not exists
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'GuzDB')
BEGIN
    CREATE DATABASE GuzDB;
END
GO

-- Use the database
USE GuzDB;
GO

-- Enable identity insert
SET IDENTITY_INSERT dbo.[user] ON;
GO

-- Create User table (Hibernate will create tables, but we ensure database exists)
-- Hibernate will handle schema creation via spring.jpa.hibernate.ddl-auto=update
