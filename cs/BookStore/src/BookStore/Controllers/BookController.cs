using System;
using System.Collections.Generic;
using System.Linq;
using Microsoft.AspNetCore.Mvc;

namespace BookStore.Controllers
{
    [Route("api/book")]
    public class BookController : Controller
    {
        readonly IEnumerable<Book> books = new Book[]
        {
            new Book() { Id = "1", Name = "Far Rainbow", Created = DateTime.Today, ReferralCount = 347 },
            new Book() { Id = "2", Name = "Hard to be a God", Created = DateTime.Today, ReferralCount = 905 },
            new Book() { Id = "3", Name = "Ugly Swans", Created = DateTime.Today, ReferralCount = 561 }
        };

        [HttpGet]
        public IEnumerable<Book> Get()
        {
            return books;
        }

        [HttpGet("{id}", Name = "GetBook")]
        public IActionResult GetById(string id)
        {
            var book = books.FirstOrDefault(b =>
            {
                return b.Id.Equals(id);
            });

            if (book == null)
            {
                return NotFound();
            }

            return new ObjectResult(book);
        }
    }
}
