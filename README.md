# Stackexchange searcher

Searching questions in Stackexchange sites.

## Stackexchange API Documentation

[Stackexchange API] (http://api.stackexchange.com/docs/search#order=desc&sort=activity&intitle=java&filter=default&site=stackoverflow&run=true)

## Build & Run

gradle bootJar
java -jar build/libs/searcher-1.0.jar

## Usage

Swagger documentation available on http://localhost:8080/documentation

By default special filter will be applied for items to include only the following item fields:
- owner.display_name;
- is_answered;
- creation_date;
- link;
- title.


### Examples

Request: 
`GET http://localhost:8080/search/?title=question&page=1&site=stackoverflow&pagesize=2`


Response:
```
{
    "items": [
        {
            "owner": {
                "display_name": "Zacth"
            },
            "is_answered": true,
            "creation_date": 1563201587,
            "link": "https://stackoverflow.com/questions/57042095/i-have-a-hw-question-that-i-cant-find-the-error-to-as-i-am-able-to-print-someon",
            "title": "I have a hw question that I cant find the error to as I am able to print. Someone able to assist me?"
        },
        {
            "owner": {
                "display_name": "Jim Hunter"
            },
            "is_answered": false,
            "creation_date": 1563200509,
            "link": "https://stackoverflow.com/questions/57041805/question-about-curly-curly-use-in-rlang-4-0",
            "title": "Question about curly-curly &#39;{{ }}&#39; use in rlang 4.0"
        }
    ],
    "has_more": true,
    "page": 1,
    "page_size": 2,
    "total": 21633
}
```

Request:

`GET http://localhost:8080/search/?title=question&page=1&site=stackoverflow&pagesize=2&filter=default`

Response:
```
{
       "items": [
           {
               "tags": [
                   "python"
               ],
               "owner": {
                   "reputation": 6,
                   "user_id": 11767413,
                   "user_type": "registered",
                   "profile_image": "https://lh6.googleusercontent.com/-ZkIU8xPrtL8/AAAAAAAAAAI/AAAAAAAAAAA/ACHi3rfUMVpLSORp84ntnsQS78lmvQ5kZg/photo.jpg?sz=128",
                   "display_name": "Zacth",
                   "link": "https://stackoverflow.com/users/11767413/zacth"
               },
               "is_answered": true,
               "view_count": 32,
               "answer_count": 3,
               "score": 1,
               "last_activity_date": 1563202653,
               "creation_date": 1563201587,
               "question_id": 57042095,
               "link": "https://stackoverflow.com/questions/57042095/i-have-a-hw-question-that-i-cant-find-the-error-to-as-i-am-able-to-print-someon",
               "title": "I have a hw question that I cant find the error to as I am able to print. Someone able to assist me?"
           },
           {
               "tags": [
                   "r",
                   "rlang"
               ],
               "owner": {
                   "reputation": 26,
                   "user_id": 2002164,
                   "user_type": "registered",
                   "profile_image": "https://www.gravatar.com/avatar/7ab3f21c3a2a4b298392dace9de3c137?s=128&d=identicon&r=PG",
                   "display_name": "Jim Hunter",
                   "link": "https://stackoverflow.com/users/2002164/jim-hunter"
               },
               "is_answered": false,
               "view_count": 19,
               "answer_count": 0,
               "score": 0,
               "last_activity_date": 1563200996,
               "creation_date": 1563200509,
               "last_edit_date": 1563200996,
               "question_id": 57041805,
               "link": "https://stackoverflow.com/questions/57041805/question-about-curly-curly-use-in-rlang-4-0",
               "title": "Question about curly-curly &#39;{{ }}&#39; use in rlang 4.0"
           }
       ],
       "has_more": true,
       "quota_max": 300,
       "quota_remaining": 251
   }
   ```