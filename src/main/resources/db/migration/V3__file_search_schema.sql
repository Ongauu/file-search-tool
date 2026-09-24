CREATE TABLE folders (
     id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
     user_id        UUID NOT NULL,
     parent_id       UUID REFERENCES folders(id) ON DELETE CASCADE,
     name            VARCHAR(255) NOT NULL,
     created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
     updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
     CONSTRAINT uq_folder_name_per_parent UNIQUE (owner_id, parent_id, name)
);

CREATE INDEX idx_folders_owner ON folders(owner_id);
CREATE INDEX idx_folders_parent ON folders(parent_id);

CREATE TABLE files (
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id   UUID NOT NULL,
    folder_id  UUID REFERENCES folders(id) ON DELETE SET NULL,
    filename  VARCHAR(512) NOT NULL,
    content_type  VARCHAR(255),
    file_extension  VARCHAR(32),
    size_bytes BIGINT  NOT NULL,
    storage_object_key  VARCHAR(1024) NOT NULL UNIQUE,
    checksum_sha256  VARCHAR(64),
    extracted_content    TEXT,
    content_tsv   TSVECTOR,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at   TIMESTAMPTZ
);


CREATE FUNCTION files_tsv_trigger() RETURNS trigger AS $$
BEGIN
    NEW.content_tsv :=
        setweight(to_tsvector('english', coalesce(NEW.filename, '')), 'A') ||
        setweight(to_tsvector('english', coalesce(NEW.extracted_content, '')), 'B');
RETURN NEW;
END
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_files_tsv
    BEFORE INSERT OR UPDATE ON files
                         FOR EACH ROW EXECUTE FUNCTION files_tsv_trigger();

CREATE INDEX idx_files_content_tsv ON files USING GIN (content_tsv);
