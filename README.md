# DDL Data Seeder

> **⚠️ WARNING — TESTING ONLY**
>
> This project is intended **strictly for local testing/benchmarking**. **Do NOT deploy or use in production.**

This project provides a user interface to execute DDL statements, detect created tables.  
And perform bulk data insertion for testing high-volume queries.

<img width="100%" alt="image" src="https://github.com/user-attachments/assets/e143721b-9075-4dfa-96db-8501813c15d9">

## Features

- DDL panel in the UI
- Schema & Table inspector (table dropdown + column metadata)
- Bulk insertion controls: **Rows, Batch size, Concurrency**, plus **client‑side chunking & progress bar**
- Theme toggle: **Dark / Light**
- **MySQL only**

## How to Run

1. Clone this repository.
2. Navigate to the project directory.
3. Start the application using Docker Compose:
   ```bash
   docker-compose up --build
   ```
    - **Default MySQL Configuration**
        - Version: `8.0`
        - Port: `3306`
        - Database: `playground`
        - Username: `root`
        - Password: `root`
        - You can change these credentials in the `.env` file.
4. Access the application at: http://localhost:8080

## Usage

1) **Execute DDL**
    - Open the *Execute DDL* panel, paste SQL (you can separate multiple statements with `;`), then click Execute DDL.
    - On success, the app **refreshes the schema** so new tables appear in the dropdown.

2) **Schema & Table Selection**
    - Use the **Tables** dropdown (and **Refresh**) to pick a table.
    - The **Selected table columns** grid shows name/type/nullable/PK/AUTO/default.
    - Make sure your JDBC URL targets the right schema (see *Prerequisites*).

3) **Per‑column generator rules**
    - Each column renders as a **card**: left = column meta, right = generator dropdown + options.
    - Click **Apply auto mapping** to pre‑fill reasonable generators based on column name/type.
    - Supported generators (UI options):
        - `AUTO` — omit from INSERT (use DB auto‑increment/default).
        - `const` — fixed value (e.g., status = `ACTIVE`).
        - `name` — human‑readable names.
        - `email` — emails (optional domain).
        - `lorem` — text (words/max length).
        - `int` — integer range.
        - `decimal` — decimal range with `scale`.
        - `datetime` — random timestamp within a range.
        - `pick` — pick from a list; optional **weights** bias the probability (weights length must match values).

4) **Bulk Data Insertion**
    - **Target table** defaults to your selection.
    - Set **Rows to insert**, **Batch size**, **Concurrency**.
    - Enable **Progress (client‑side)** to split into multiple requests and show a **progress bar** and live **rows/s**.
    - Click **Run bulk insert** to start.

5) **Theme**
    - Use the **Dark/Light** toggle at the top‑right.
