import subprocess
import tempfile
import xml.etree.ElementTree as ElementTree
from io import BytesIO
from pathlib import Path
from zipfile import BadZipFile, ZipFile

import pdfplumber

WORD_NAMESPACE = "http://schemas.openxmlformats.org/wordprocessingml/2006/main"


class ResumeParsingError(ValueError):
    """Raised when the uploaded resume format cannot be parsed."""


def parse_resume_file(file_bytes: bytes, file_name: str) -> str:
    suffix = Path(file_name).suffix.lower()
    if suffix == ".pdf":
        return parse_pdf(file_bytes)
    if suffix == ".docx":
        return parse_docx(file_bytes)
    if suffix == ".doc":
        return parse_doc(file_bytes)
    return file_bytes.decode("utf-8", errors="ignore").strip()


def parse_pdf(file_bytes: bytes) -> str:
    with pdfplumber.open(BytesIO(file_bytes)) as pdf:
        pages = [page.extract_text() or "" for page in pdf.pages]
    return "\n".join(pages).strip()


def parse_docx(file_bytes: bytes) -> str:
    try:
        with ZipFile(BytesIO(file_bytes)) as document:
            xml_content = document.read("word/document.xml")
    except (BadZipFile, KeyError) as exc:
        raise ResumeParsingError("Could not parse DOCX resume file") from exc

    root = ElementTree.fromstring(xml_content)
    paragraphs = []
    for paragraph in root.iter(f"{{{WORD_NAMESPACE}}}p"):
        text = "".join(
            node.text or "" for node in paragraph.iter(f"{{{WORD_NAMESPACE}}}t")
        ).strip()
        if text:
            paragraphs.append(text)
    return "\n".join(paragraphs).strip()


def parse_doc(file_bytes: bytes) -> str:
    file_path = None
    try:
        with tempfile.NamedTemporaryFile(suffix=".doc", delete=False) as document:
            document.write(file_bytes)
            file_path = document.name
        result = subprocess.run(
            ["antiword", file_path],
            capture_output=True,
            check=False,
            timeout=20,
        )
        if result.returncode != 0:
            raise ResumeParsingError("Could not parse DOC resume file")
        return result.stdout.decode("utf-8", errors="ignore").strip()
    except FileNotFoundError as exc:
        raise ResumeParsingError("DOC parser is not installed") from exc
    except subprocess.TimeoutExpired as exc:
        raise ResumeParsingError("DOC resume parsing timed out") from exc
    finally:
        if file_path is not None:
            Path(file_path).unlink(missing_ok=True)
