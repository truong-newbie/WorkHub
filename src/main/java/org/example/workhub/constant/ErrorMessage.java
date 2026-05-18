package org.example.workhub.constant;

public class ErrorMessage {

  public static final String ERR_EXCEPTION_GENERAL = "exception.general";
  public static final String UNAUTHORIZED = "exception.unauthorized";
  public static final String FORBIDDEN = "exception.forbidden";
  public static final String FORBIDDEN_UPDATE_DELETE = "exception.forbidden.update-delete";

  //error validation dto
  public static final String INVALID_SOME_THING_FIELD = "invalid.general";
  public static final String INVALID_FORMAT_SOME_THING_FIELD = "invalid.general.format";
  public static final String INVALID_SOME_THING_FIELD_IS_REQUIRED = "invalid.general.required";
  public static final String NOT_BLANK_FIELD = "invalid.general.not-blank";
  public static final String INVALID_FORMAT_PASSWORD = "invalid.password-format";
  public static final String INVALID_DATE = "invalid.date-format";
  public static final String INVALID_DATE_FEATURE = "invalid.date-future";
  public static final String INVALID_DATETIME = "invalid.datetime-format";
  public static final String INVALID_EMAIL="invalid.email";
  public static final String INVALID_PASSWORD="invalid.password";
  public static final String INVALID_OTP= "invalid.otp";
  public static final String OTP_EXPIRED= "otp.expired";
  public static final String INVALID_REPEAT_PASSWORD="invalid.repeat.password";

  public static class Auth {
    public static final String ERR_INCORRECT_EMAIL = "exception.auth.incorrect.email";
    public static final String ERR_INCORRECT_PASSWORD = "exception.auth.incorrect.password";
    public static final String ERR_ACCOUNT_NOT_ENABLED = "exception.auth.account.not.enabled";
    public static final String ERR_ACCOUNT_LOCKED = "exception.auth.account.locked";
    public static final String INVALID_REFRESH_TOKEN = "exception.auth.invalid.refresh.token";
    public static final String EXPIRED_REFRESH_TOKEN = "exception.auth.expired.refresh.token";
    public static final String ERR_ALREADY_EXISTS_EMAIL = "exception.auth.already.exists.email";
    public static final String ERR_ALREADY_LOGGED_IN="exception.auth.already.logged";
  }

  public static class User {
    public static final String ERR_NOT_FOUND_USERNAME = "exception.user.not.found.username";
    public static final String ERR_NOT_FOUND_ID = "exception.user.not.found.id";
    public static final String ERR_NOT_FOUND_EMAIL = "exception.user.not.found.email";
    public static final String ERR_ALREADY_EXISTS_USERNAME = "exception.user.already.exists.username";
  }
  public static class Role{
    public static final String ERR_NOT_FOUND = "exception.role.not.found";
  }
  public static class Company{
    public static final String ERR_NOT_FOUND = "exception.company.not.found";
    public static final String ERR_ALREADY_EXISTS_COMPANY = "exception.company.already.exists.company";
    public static final String ERR_PERMISSION_DENIED = "company.permission.denied";
    public static final String ERR_INVALID_WEBSITE = "company.invalid.website";
    public static final String ERR_INVALID_EMAIL = "company.invalid.email";
    public static final String ERR_INVALID_PHONE = "company.invalid.phone";
    public static final String ERR_ALREADY_APPROVED = "company.already.approved";
    public static final String ERR_ALREADY_DISABLED = "company.already.disabled";
    public static final String ERR_OWNER_NOT_FOUND = "company.owner.not.found";
    public static final String ERR_FILE_EMPTY = "company.file.empty";
    public static final String ERR_FILE_INVALID = "company.file.invalid";
  }

  public static class Skill{
    public static final String ERR_NOT_FOUND = "exception.skill.not.found";
    public static final String ERR_ALREADY_EXISTS_SKILL= "exception.skill.already.exists.skill";
    public static final String ERR_PERMISSION_DENIED = "skill.permission.denied";
    public static final String ERR_SLUG_EXISTS = "skill.slug.exists";
    public static final String ERR_ALREADY_ENABLED = "skill.already.enabled";
    public static final String ERR_ALREADY_DISABLED = "skill.already.disabled";
    public static final String ERR_INACTIVE = "skill.inactive";
    public static final String ERR_DELETED = "skill.deleted";
    public static final String ERR_IN_USE = "skill.in.use";
  }

  public static class Job{
    public static final String ERR_NOT_FOUND_ID = "exception.job.not.found";
    public static final String ERR_ALREADY_EXISTS_SLUG = "exception.job.already.exists.slug";
    public static final String ERR_PERMISSION_DENIED = "exception.job.permission.denied";
    public static final String ERR_EXPIRED_INVALID = "exception.job.expired.invalid";
    public static final String ERR_SALARY_INVALID = "exception.job.salary.invalid";
  }

  public static class Application{
    public static final String ERR_NOT_FOUND_ID = "exception.application.not.found";
    public static final String ERR_ALREADY_APPLIED = "exception.application.already.applied";
    public static final String ERR_CANNOT_WITHDRAW = "exception.application.cannot.withdraw";
  }

  public static class Favorite{
    public static final String ERR_ALREADY_FAVORITE = "exception.favorite.already.exists";
    public static final String ERR_NOT_FAVORITE = "exception.favorite.not.found";
  }

  public static class Resume{
    public static final String ERR_NOT_FOUND = "resume.not.found";
    public static final String ERR_FILE_INVALID = "resume.file.invalid";
    public static final String ERR_FILE_TOO_LARGE = "resume.file.too.large";
    public static final String ERR_PERMISSION_DENIED = "resume.permission.denied";
    public static final String ERR_UPLOAD_EMPTY = "resume.upload.empty";
    public static final String ERR_DUPLICATE_TITLE = "resume.title.duplicate";
    public static final String ERR_ATS_SCORE_INVALID = "resume.ats.score.invalid";
    public static final String ERR_UPLOAD_FAILED = "resume.upload.failed";
  }

  public static class Subscriber{
    public static final String ERR_NOT_FOUND = "subscriber.not.found";
    public static final String ERR_PERMISSION_DENIED = "subscriber.permission.denied";
    public static final String ERR_EMAIL_EXISTS = "subscriber.email.exists";
    public static final String ERR_INVALID_EMAIL = "subscriber.invalid.email";
    public static final String ERR_SKILL_EMPTY = "subscriber.skill.empty";
    public static final String ERR_INVALID_UNSUBSCRIBE_TOKEN = "subscriber.unsubscribe.token.invalid";
  }

  public static class Assessment {
    public static final String ERR_TEST_NOT_FOUND = "exception.assessment.test.not.found";
    public static final String ERR_QUESTION_NOT_FOUND = "exception.assessment.question.not.found";
    public static final String ERR_ASSIGNMENT_NOT_FOUND = "exception.assessment.assignment.not.found";
    public static final String ERR_ANSWER_NOT_FOUND = "exception.assessment.answer.not.found";
    public static final String ERR_NOT_OWNER = "exception.assessment.not.owner";
    public static final String ERR_TEST_NOT_PUBLISHED = "exception.assessment.test.not.published";
    public static final String ERR_TEST_EXPIRED = "exception.assessment.test.expired";
    public static final String ERR_ALREADY_SUBMITTED = "exception.assessment.already.submitted";
    public static final String ERR_ALREADY_ASSIGNED = "exception.assessment.already.assigned";
    public static final String ERR_INVALID_QUESTION = "exception.assessment.invalid.question";
    public static final String ERR_INVALID_OPTION = "exception.assessment.invalid.option";
    public static final String ERR_INVALID_SCORE = "exception.assessment.invalid.score";
    public static final String ERR_NO_QUESTIONS = "exception.assessment.no.questions";
    public static final String ERR_MC_OPTIONS_REQUIRED = "exception.assessment.multiple.choice.options.required";
    public static final String ERR_MC_CORRECT_REQUIRED = "exception.assessment.multiple.choice.correct.required";
    public static final String ERR_DURATION_EXCEEDED = "exception.assessment.duration.exceeded";
    public static final String ERR_INVALID_ASSIGNMENT_APPLICATION = "exception.assessment.invalid.assignment.application";
    public static final String ERR_INVALID_ASSIGNMENT_STATUS = "exception.assessment.invalid.assignment.status";
    public static final String ERR_DUPLICATE_ANSWER = "exception.assessment.duplicate.answer";
  }

}
