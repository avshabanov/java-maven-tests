using System;

namespace BookStore
{
    public class Book
    {
        public string Id { get; set; }
        public string Name { get; set; }
        public DateTime Created { get; set; }
        public int ReferralCount { get; set; }
    }
}
