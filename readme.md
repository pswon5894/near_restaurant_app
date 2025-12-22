근처 식당을 찾아서 확인해주는 안드로이드 코틀린 앱

기능
위치정보를 통해 근처 식당 확인
근처식당의 평가및 앱 내에서 정보 공유 및 즐겨 찾기 등록

https://bj-turtle.tistory.com/100
뷰바인딩

//ContextCompat은 Resource에서 값을 가져오거나 퍼미션을 확인할 때 사용할 때 SDK버전을 고려하지 않아도 되도록 (내부적으로 SDK버전을 처리해둔) 클래스

why 레트로핏?
요청 바디값(request body)과 응답 바디값(response body)을 원하는 타입으로 안전하게 바꾸어주기 떄문에 타입안저
네트워킹 관련 스레딩, 에러 핸들링, 응답 파싱 등에 필요한 보일러 플레이트 코드 줄임

레트로핏 라이브러리 구성요소
인터페이스:http메서드 정의
레트로핏 클래스: 레트로핏 객체 생성
데이터 클래스:json 데이트를 담는

//https://developers.google.com/maps/documentation/places/web-service/legacy/details?hl=ko#PlacePhoto
//구글 place api 기존

Place API는 Nearby Search(근처 검색), Text Search(텍스트 검색),  Place Details (장소 세부 정보), Place Photos (장소 사진), Autocomplete (장소 자동 완성)
https://www.youtube.com/watch?v=VI4fnizbQ2c

https://developers.google.com/maps/documentation/places/android-sdk/place-photos?hl=ko&_gl=1*1bnd0st*_up*MQ..*_ga*MTM2NDgwNjI0LjE3NjU0MTc0MDE.*_ga_SM8HXJ53K2*czE3NjU0MTc0MDEkbzEkZzAkdDE3NjU0MTc0MDEkajYwJGwwJGgw*_ga_NRWSTWS78N*czE3NjU0MTc0MDEkbzEkZzEkdDE3NjU0MTc0NTgkajMkbDAkaDA.