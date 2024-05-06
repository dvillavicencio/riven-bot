package com.deahtstroke.rivenbot.dto.destiny;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchResult {

  private List<UserSearchResult> searchResults;

  private Integer page;

  private Boolean hasMore;
}
