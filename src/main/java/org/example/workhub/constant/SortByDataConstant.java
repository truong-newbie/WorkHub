package org.example.workhub.constant;

public enum SortByDataConstant implements SortByInterface {

  USER {
    @Override
    public String getSortBy(String sortBy) {
      switch (sortBy) {
        case "firstName":
          return "firstName";
        case "lastName":
          return "lastName";
        case "lastModifiedDate":
          return "lastModifiedDate";
        default:
          return "createdDate";
      }
    }
  },

  COMPANY {
    @Override
    public String getSortBy(String sortBy) {
      switch (sortBy) {
        case "name":
          return "name";
        case "address":
          return "address";
        case "createdDate":
          return "createdDate";
        default:
          return "createdDate";
      }
    }
  }

}


