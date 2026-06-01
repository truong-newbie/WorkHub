import unittest
from io import BytesIO
from zipfile import ZipFile

from app.services.parser_service import ResumeParsingError, parse_resume_file


def build_docx(*paragraphs: str) -> bytes:
    content = "".join(
        f"<w:p><w:r><w:t>{paragraph}</w:t></w:r></w:p>"
        for paragraph in paragraphs
    )
    xml = (
        '<?xml version="1.0" encoding="UTF-8"?>'
        '<w:document xmlns:w="http://schemas.openxmlformats.org/'
        'wordprocessingml/2006/main">'
        f"<w:body>{content}</w:body>"
        "</w:document>"
    )
    buffer = BytesIO()
    with ZipFile(buffer, "w") as document:
        document.writestr("word/document.xml", xml)
    return buffer.getvalue()


class ParserServiceTest(unittest.TestCase):
    def test_txt_resume_is_decoded(self):
        self.assertEqual(
            "Java developer",
            parse_resume_file(b" Java developer ", "resume.txt"),
        )

    def test_docx_resume_extracts_paragraph_text(self):
        result = parse_resume_file(
            build_docx("Java Spring Boot developer", "Built REST APIs"),
            "resume.docx",
        )

        self.assertEqual("Java Spring Boot developer\nBuilt REST APIs", result)

    def test_invalid_docx_fails_clearly(self):
        with self.assertRaisesRegex(ResumeParsingError, "Could not parse DOCX"):
            parse_resume_file(b"not-a-docx", "resume.docx")


if __name__ == "__main__":
    unittest.main()

