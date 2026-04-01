package org.example.workhub.constant;

public enum SortByDataConstant implements SortByInterface {

//  USER {
//    @Override
//    public String getSortBy(String sortBy) {
//      switch (sortBy) {
//        case "firstName":
//          return "first_name";
//        case "lastName":
//          return "last_name";
//        case "lastModifiedDate":
//          return "last_modified_date";
//        default:
//          return "created_date";
//      }
//    }
//  },
USER {
  @Override
  public String getSortBy(String sortBy) {

    switch (sortBy) {
      case "name":
        return "name";
      case "email":
        return "email";
      case "address":
        return "address";
      case "lastModifiedDate":
        return "lastModifiedDate";
      default:

        return "createdDate";
    }
  }
},

}
